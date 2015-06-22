
package com.ksyun.ks3.service.encryption.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.LengthCheckInputStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.Mimetypes;
import com.ksyun.ks3.service.encryption.S3Direct;
import com.ksyun.ks3.service.encryption.model.CryptoConfiguration;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;
import com.ksyun.ks3.service.encryption.model.MaterialsDescriptionProvider;
import com.ksyun.ks3.service.request.AbortMultipartUploadRequest;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.utils.Base64;

/**
 * Common implementation for different S3 cryptographic modules.
 */
public abstract class S3CryptoModuleBase<T extends MultipartUploadContext>
        extends S3CryptoModule<T> {
    protected static final int DEFAULT_BUFFER_SIZE = 1024*2;    // 2K
    protected final EncryptionMaterialsProvider kekMaterialsProvider;
    protected final CryptoConfiguration cryptoConfig;
    protected final Log log = LogFactory.getLog(getClass());
    protected final S3CryptoScheme cryptoScheme;

    protected final ContentCryptoScheme contentCryptoScheme;

    /** Map of data about in progress encrypted multipart uploads. */
    protected final  Map<String, T> multipartUploadContexts =
        Collections.synchronizedMap(new HashMap<String,T>());
    protected final S3Direct s3;
    
    protected S3CryptoModuleBase(S3Direct s3,
            EncryptionMaterialsProvider kekMaterialsProvider,
            CryptoConfiguration cryptoConfig,
            S3CryptoScheme cryptoScheme) {
        this.kekMaterialsProvider = kekMaterialsProvider;
        this.cryptoConfig = cryptoConfig;
        this.s3 = s3;
        this.cryptoScheme = cryptoScheme;
        this.contentCryptoScheme = cryptoScheme.getContentCryptoScheme();
    }

    /**
     * Returns the length of the ciphertext computed from the length of the
     * plaintext.
     * 
     * @param plaintextLength
     *            a non-negative number
     * @return a non-negative number
     */
    protected abstract long ciphertextLength(long plaintextLength);

    //////////////////////// Common Implementation ////////////////////////
    @Override
    public final void abortMultipartUploadSecurely(AbortMultipartUploadRequest req) {
        s3.abortMultipartUpload(req);
        multipartUploadContexts.remove(req.getUploadId());
    }

    protected final ObjectMetadata updateMetadataWithContentCryptoMaterial(
            ObjectMetadata metadata, File file, ContentCryptoMaterial instruction) {
        if (metadata == null) 
            metadata = new ObjectMetadata();
        if (file != null) {
            Mimetypes mimetypes = Mimetypes.getInstance();
            metadata.setContentType(mimetypes.getMimetype(file));
        }
        return instruction.toObjectMetadata(metadata);
    }
    
    protected final ContentCryptoMaterial createContentCryptoMaterial(Ks3WebServiceRequest req) {
        if (req instanceof MaterialsDescriptionProvider) {
            return newContentCryptoMaterial(this.kekMaterialsProvider, 
                    ((MaterialsDescriptionProvider) req).getMaterialsDescription(), 
                    this.cryptoConfig.getCryptoProvider());
        } else {
            return newContentCryptoMaterial(this.kekMaterialsProvider, this.cryptoConfig.getCryptoProvider());
        }
    }

    /**
     * Generates and returns the content encryption material with the given kek
     * material, material description and security providers.
     */
    private ContentCryptoMaterial newContentCryptoMaterial(
            EncryptionMaterialsProvider kekMaterialProvider,
            Map<String, String> materialsDescription, Provider provider) {
        EncryptionMaterials kekMaterials = kekMaterialProvider.getEncryptionMaterials(materialsDescription);
        return buildContentCryptoMaterial(kekMaterials, provider);
    }

    /**
     * Generates and returns the content encryption material with the given kek
     * material and security providers.
     */
    private ContentCryptoMaterial newContentCryptoMaterial(
            EncryptionMaterialsProvider kekMaterialProvider,
            Provider provider) {
        EncryptionMaterials kekMaterials = kekMaterialProvider.getEncryptionMaterials();
        return buildContentCryptoMaterial(kekMaterials, provider);
    }
    
    private ContentCryptoMaterial buildContentCryptoMaterial(
            EncryptionMaterials kekMaterials, Provider provider) {
        // Generate a one-time use symmetric key and initialize a cipher to encrypt object data
        SecretKey cek = generateCEK();
        // Randomly generate the IV
        byte[] iv = new byte[contentCryptoScheme.getIVLengthInBytes()];
        cryptoScheme.getSecureRandom().nextBytes(iv);
        // Encrypt the envelope symmetric key
        SecuredCEK cekSecured = secureCEK(cek, kekMaterials, provider);
        // Return a new instruction with the appropriate fields.
        return new ContentCryptoMaterial(
            kekMaterials.getMaterialsDescription(),
            cekSecured.encrypted, 
            cekSecured.keyWrapAlgorithm,
            contentCryptoScheme.createCipherLite
                (cek, iv,Cipher.ENCRYPT_MODE, provider));
        
    }

    protected final SecretKey generateCEK() {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance(contentCryptoScheme
                    .getKeyGeneratorAlgorithm());
            generator.init(contentCryptoScheme.getKeyLengthInBits(),
                    cryptoScheme.getSecureRandom());
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new Ks3ClientException(
                    "Unable to generate envelope symmetric key:"
                            + e.getMessage(), e);
        }
    }

    protected final SecuredCEK secureCEK(SecretKey toBeEncrypted,
            EncryptionMaterials materials, Provider cryptoProvider)
    {
        Key kek; 
        if (materials.getKeyPair() != null) {
            // Do envelope encryption with public key from key pair
            kek = materials.getKeyPair().getPublic();
        } else {
            // Do envelope encryption with symmetric key
            kek= materials.getSymmetricKey();
        }
        S3KeyWrapScheme kwScheme = cryptoScheme.getKeyWrapScheme();
        String keyWrapAlgo = kwScheme.getKeyWrapAlgorithm(kek);
        try {
            if (keyWrapAlgo != null) {
                Cipher cipher = cryptoProvider == null 
                    ? Cipher.getInstance(keyWrapAlgo)
                    : Cipher.getInstance(keyWrapAlgo, cryptoProvider)
                    ;
                cipher.init(Cipher.WRAP_MODE, kek, cryptoScheme.getSecureRandom());
                return new SecuredCEK(cipher.wrap(toBeEncrypted), keyWrapAlgo);
            }
            // fall back to the Encryption Only (EO) key encrypting method
            Cipher cipher;
            byte[] toBeEncryptedBytes = toBeEncrypted.getEncoded();
            String algo = kek.getAlgorithm();
            if (cryptoProvider != null) {
                cipher = Cipher.getInstance(algo, cryptoProvider);
            } else {
                cipher = Cipher.getInstance(algo); // Use default JCE Provider
            }
            cipher.init(Cipher.ENCRYPT_MODE, kek);
            return new SecuredCEK(cipher.doFinal(toBeEncryptedBytes), null);
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to encrypt symmetric key: " + e.getMessage(), e);
        }
    }

    /** Used to carry both the secured CEK and the key wrapping algorithm, if any. */
    private static class SecuredCEK {
        /**
         * The encrypted CEK either via key wrapping or simple encryption.
         */
        final byte[] encrypted;
        /**
         * The key wrapping algorithm used, or null if the CEK is not secured
         * via key wrapping.
         */
        final String keyWrapAlgorithm;
        SecuredCEK(byte[] encryptedKey, String keyWrapAlgorithm) {
            this.encrypted = encryptedKey;
            this.keyWrapAlgorithm = keyWrapAlgorithm;
        }
    }

    /**
     * Returns a request that has the content as input stream wrapped with a
     * cipher, and configured with some meta data and user metadata.
     */
    protected final PutObjectRequest wrapWithCipher(
            PutObjectRequest request, ContentCryptoMaterial cekMaterial) {
        // Create a new metadata object if there is no metadata already.
        ObjectMetadata metadata = request.getObjectMeta();
        if (metadata == null) {
            metadata = new ObjectMetadata();
        }

        // Record the original Content MD5, if present, for the unencrypted data
        if (metadata.getContentMD5() != null) {
            metadata.setUserMeta(HttpHeaders.UNENCRYPTED_CONTENT_MD5.toString(),
                    metadata.getContentMD5());
        }

        // Removes the original content MD5 if present from the meta data.
        metadata.setContentMD5(null);

        // Record the original, unencrypted content-length so it can be accessed
        // later
        final long plaintextLength = plaintextLength(request, metadata);
        if (plaintextLength >= 0) {
            metadata.setUserMeta(HttpHeaders.UNENCRYPTED_CONTENT_LENGTH.toString(),
                    Long.toString(plaintextLength));
            // Put the ciphertext length in the metadata
            metadata.setContentLength(ciphertextLength(plaintextLength));
        }
        request.setObjectMeta(metadata);
        request.setInputStream(newS3CipherLiteInputStream(
            request, cekMaterial, plaintextLength));
        // Treat all encryption requests as input stream upload requests, not as
        // file upload requests.
        request.setFile(null);
        return request;
    }

    private CipherLiteInputStream newS3CipherLiteInputStream(
            PutObjectRequest req, ContentCryptoMaterial cekMaterial,
            long plaintextLength) {
        try {
            InputStream is = req.getInputStream();
            if (req.getFile() != null)
                is = new RepeatableFileInputStream(req.getFile());
            if (plaintextLength > -1) {
                // S3 allows a single PUT to be no more than 5GB, which
                // therefore won't exceed the maximum length that can be
                // encrypted either using any cipher such as CBC or GCM.
                
                // This ensures the plain-text read from the underlying data
                // stream has the same length as the expected total.
                is = new LengthCheckInputStream(is, plaintextLength,
                		LengthCheckInputStream.EXCLUDE_SKIPPED_BYTES);
            }
            return new CipherLiteInputStream(is,
                    cekMaterial.getCipherLite(),
                    DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new Ks3ClientException(
                    "Unable to create cipher input stream: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Returns the plaintext length from the request and metadata; or -1 if
     * unknown.
     */
    protected final long plaintextLength(PutObjectRequest request,
            ObjectMetadata metadata) {
    	long fileLength = -1;
    	long lengthInMeta = request.getObjectMeta().getContentLength();
    	if (request.getFile() != null) {
    		fileLength = request.getFile().length();
        }
    	if(fileLength>=0){
    		return fileLength;
    	}else if(lengthInMeta > 0&&request.getInputStream()!=null){
    		return lengthInMeta;
    	}
        return -1;
    }

    public final S3CryptoScheme getS3CryptoScheme() {
        return cryptoScheme;
    }

    /**
     * Updates put request to store the specified instruction object in S3.
     *
     * @param request
     *      The put request for the original object to be stored in S3.
     * @param cekMaterial
     *      The instruction object to be stored in S3.
     * @return
     *      A put request to store the specified instruction object in S3.
     */
    protected final PutObjectRequest upateInstructionPutRequest(
            PutObjectRequest request, ContentCryptoMaterial cekMaterial) {
        byte[] bytes = cekMaterial.toJsonString().getBytes(Charset.forName("UTF-8"));
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = request.getObjectMeta();
        if (metadata == null) {
            metadata = new ObjectMetadata();
            request.setObjectMeta(metadata);
        }
        // Set the content-length of the upload
        metadata.setContentLength(bytes.length);
        //不能使用之前request的content-md5
        metadata.setContentMD5(null);
        // Set the crypto instruction file header
        metadata.setUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString(), Base64.encodeAsString((request.getKey() + EncryptionUtils.INSTRUCTION_SUFFIX).getBytes()));
        // Update the instruction request
        request.setKey(request.getKey() + EncryptionUtils.INSTRUCTION_SUFFIX);
        request.setObjectMeta(metadata);
        request.setInputStream(is);
        request.setFile(null);
        return request;
    }

    protected final PutObjectRequest createInstructionPutRequest(
            String bucketName, String key, ContentCryptoMaterial cekMaterial) {
        byte[] bytes = cekMaterial.toJsonString().getBytes(Charset.forName("UTF-8"));
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        //亚马逊是空的，但是由于签名问题，加了个值
        metadata.setUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString(),Base64.encodeAsString((key + EncryptionUtils.INSTRUCTION_SUFFIX).getBytes()));
        return new PutObjectRequest(bucketName, key + EncryptionUtils.INSTRUCTION_SUFFIX,
                is, metadata);
    }

    /**
     * Appends a user agent to the request's USER_AGENT client marker.
     * This method is intended only for internal use by the KS3 SDK. 
     */
    final <X extends Ks3WebServiceRequest> X appendUserAgent(
            X request, String userAgent) {
        request.getRequestConfig().setUserAgent( userAgent);
        return request;
    }
}
