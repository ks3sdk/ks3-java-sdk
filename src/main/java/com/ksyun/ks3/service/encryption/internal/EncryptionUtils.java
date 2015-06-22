
package com.ksyun.ks3.service.encryption.internal;

import java.io.BufferedReader;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.InputSubStream;
import com.ksyun.ks3.LengthCheckInputStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.utils.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.Mimetypes;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsAccessor;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;
import com.ksyun.ks3.service.encryption.model.StaticEncryptionMaterialsProvider;
import com.ksyun.ks3.service.request.DeleteObjectRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.JSONObject;
import com.ksyun.ks3.utils.Jackson;
import com.ksyun.ks3.utils.StringUtils;


/**
 * The EncryptionUtils class encrypts and decrypts data stored in S3.  It can be used to prepare
 * requests for encryption before they are stored in S3 and to decrypt objects that are retrieved from S3.
 */
public class EncryptionUtils {

    /** Suffix appended to the end of instruction file names */
    public static final String INSTRUCTION_SUFFIX = ".instruction";

    /**
     * Returns an updated request where the metadata contains encryption information and the input stream contains
     * the encrypted object contents.  The specified encryption materials will be used to encrypt and decrypt data.
     *
     * @param request
     *      The request whose contents are to be encrypted.
     * @param materials
     *      The encryption materials to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt data
     * @return
     *      The updated request where the metadata is set up for encryption and input stream contains
     *      the encrypted contents.
     *
     * @deprecated use generateInstruction, encryptRequestUsingInstruction, and updateMetadataWithEncryptionInfo instead
     */
    @Deprecated
    public static PutObjectRequest encryptRequestUsingMetadata(PutObjectRequest request, EncryptionMaterials materials, Provider cryptoProvider) {
        // Create instruction
        EncryptionInstruction instruction = EncryptionUtils.generateInstruction(materials, cryptoProvider);

        // Encrypt the object data with the instruction
        PutObjectRequest encryptedObjectRequest = EncryptionUtils.encryptRequestUsingInstruction(request, instruction);

        // Update the metadata
        EncryptionUtils.updateMetadataWithEncryptionInstruction( request, instruction );

        return encryptedObjectRequest;
    }

    /**
     * Returns an updated object where the object content input stream contains the decrypted contents.
     *
     * @param object
     *      The object whose contents are to be decrypted.
     * @param materials
     *      The encryption materials to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to decrypt data
     * @return
     *      The updated object where the object content input stream contains the decrypted contents.
     *
     * @deprecated use buildInstructionFromObjectMetadata and decryptObjectUsingInstruction instead.
     */
    @Deprecated
    public static Ks3Object decryptObjectUsingMetadata(Ks3Object object, EncryptionMaterials materials, Provider cryptoProvider) {
        // Create an instruction object from the object headers
        EncryptionInstruction instruction = EncryptionUtils.buildInstructionFromObjectMetadata( object, materials, cryptoProvider );

        // Decrypt the object file with the instruction
        return EncryptionUtils.decryptObjectUsingInstruction(object, instruction);
    }

    /**
     * Generates an instruction that will be used to encrypt an object.
     *
     * @param materials
     *      The encryption materials to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt and decrypt data.
     * @return
     *      The instruction that will be used to encrypt an object.
     */
    @Deprecated
    public static EncryptionInstruction generateInstruction(EncryptionMaterials materials, Provider cryptoProvider) {
      return generateInstruction(new StaticEncryptionMaterialsProvider(materials), cryptoProvider);
    }

    public static EncryptionInstruction generateInstruction(EncryptionMaterialsProvider materialsProvider,
            Provider cryptoProvider) {
        return buildInstruction(materialsProvider.getEncryptionMaterials(), cryptoProvider);
    }

    public static EncryptionInstruction generateInstruction(EncryptionMaterialsProvider materialsProvider,
            Map<String, String> materialsDescription, Provider cryptoProvider) {
        return buildInstruction(materialsProvider.getEncryptionMaterials(materialsDescription), cryptoProvider);
    }

    private static EncryptionInstruction buildInstruction(EncryptionMaterials materials, Provider cryptoProvider) {
        // Generate a one-time use symmetric key and initialize a cipher to
        // encrypt object data
        SecretKey envelopeSymmetricKey = generateOneTimeUseSymmetricKey();
        CipherFactory cipherFactory = new CipherFactory(envelopeSymmetricKey, Cipher.ENCRYPT_MODE, null, cryptoProvider);

        // Encrypt the envelope symmetric key
        byte[] encryptedEnvelopeSymmetricKey = getEncryptedSymmetricKey(envelopeSymmetricKey, materials, cryptoProvider);

        // Return a new instruction with the appropriate fields.
        return new EncryptionInstruction(materials.getMaterialsDescription(), encryptedEnvelopeSymmetricKey,
                envelopeSymmetricKey, cipherFactory);
    }

    /**
     * Builds an instruction object from the contents of an instruction file.
     *
     * @param instructionFile
     *      A non-null instruction file retrieved from S3 that contains encryption information
     * @param materials
     *      The non-null encryption materials to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt and decrypt data.  Null is ok and uses the
     *      preferred provider from Security.getProviders().
     * @return
     *      A non-null instruction object containing encryption information
     */
    @Deprecated
    public static EncryptionInstruction buildInstructionFromInstructionFile(Ks3Object instructionFile, EncryptionMaterials materials, Provider cryptoProvider) {
        return buildInstructionFromInstructionFile(instructionFile, new StaticEncryptionMaterialsProvider(materials), cryptoProvider);
    }

    /**
     * Builds an instruction object from the contents of an instruction file.
     *
     * @param instructionFile
     *      A non-null instruction file retrieved from S3 that contains encryption information
     * @param materialsProvider
     *      The non-null encryption materials provider to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt and decrypt data.  Null is ok and uses the
     *      preferred provider from Security.getProviders().
     * @return
     *      A non-null instruction object containing encryption information
     */
    public static EncryptionInstruction buildInstructionFromInstructionFile(Ks3Object instructionFile, EncryptionMaterialsProvider materialsProvider, Provider cryptoProvider) {
        JSONObject instructionJSON = parseJSONInstruction(instructionFile);
            // Get fields from instruction object
            String encryptedSymmetricKeyB64 = instructionJSON.getString(HttpHeaders.CRYPTO_KEY.toString());
            String ivB64 = instructionJSON.getString(HttpHeaders.CRYPTO_IV.toString());
            String materialsDescriptionString = instructionJSON.tryGetString(HttpHeaders.MATERIALS_DESCRIPTION.toString());
            Map<String, String> materialsDescription = convertJSONToMap(materialsDescriptionString);

            // Decode from Base 64 to standard binary bytes
            byte[] encryptedSymmetricKey = Base64.decode(encryptedSymmetricKeyB64);
            byte[] iv = Base64.decode(ivB64);

            if (encryptedSymmetricKey == null || iv == null) {
                // If necessary encryption info was not found in the instruction file, throw an exception.
                throw new Ks3ClientException(
                        String.format("Necessary encryption info not found in the instruction file '%s' in bucket '%s'",
                                      instructionFile.getKey(), instructionFile.getBucketName()));
            }

            EncryptionMaterials materials = retrieveOriginalMaterials(materialsDescription, materialsProvider);
            // If we're unable to retrieve the original encryption materials, we can't decrypt the object, so
            // throw an exception.
            if (materials == null) {
                throw new Ks3ClientException(
                        String.format("Unable to retrieve the encryption materials that originally " +
                                "encrypted object corresponding to instruction file '%s' in bucket '%s'.",
                                instructionFile.getKey(), instructionFile.getBucketName()));
            }

            // Decrypt the symmetric key and create the symmetric cipher
            SecretKey symmetricKey = getDecryptedSymmetricKey(encryptedSymmetricKey, materials, cryptoProvider);
            CipherFactory cipherFactory = new CipherFactory(symmetricKey, Cipher.DECRYPT_MODE, iv, cryptoProvider);

            return new EncryptionInstruction(materialsDescription, encryptedSymmetricKey, symmetricKey, cipherFactory);
    }

    /**
     * Builds an instruction object from the object metadata.
     *
     * @param object
     *      A non-null object that contains encryption information in its headers
     * @param materials
     *      The non-null encryption materials to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt and decrypt data.  Null is ok and uses the
     *      preferred provider from Security.getProviders().
     * @return
     *      A non-null instruction object containing encryption information
     *
     * @throws Ks3ClientException
     *      if encryption information is missing in the metadata, or the encryption
     *      materials used to encrypt the object are not available via the materials Accessor
     */
    @Deprecated
    public static EncryptionInstruction buildInstructionFromObjectMetadata(Ks3Object object, EncryptionMaterials materials, Provider cryptoProvider) {
      return buildInstructionFromObjectMetadata(object, new StaticEncryptionMaterialsProvider(materials), cryptoProvider);
    }

    /**
     * Builds an instruction object from the object metadata.
     *
     * @param object
     *      A non-null object that contains encryption information in its headers
     * @param materialsProvider
     *      The non-null encryption materials provider to be used to encrypt and decrypt data.
     * @param cryptoProvider
     *      The crypto provider whose encryption implementation will be used to encrypt and decrypt data.  Null is ok and uses the
     *      preferred provider from Security.getProviders().
     * @return
     *      A non-null instruction object containing encryption information
     *
     * @throws Ks3ClientException
     *      if encryption information is missing in the metadata, or the encryption
     *      materials used to encrypt the object are not available via the materials Accessor
     */
    public static EncryptionInstruction buildInstructionFromObjectMetadata(Ks3Object object, EncryptionMaterialsProvider materialsProvider, Provider cryptoProvider) {
        ObjectMetadata metadata = object.getObjectMetadata();

        // Get encryption info from metadata.
        byte[] encryptedSymmetricKeyBytes = getCryptoBytesFromMetadata(HttpHeaders.CRYPTO_KEY.toString(), metadata);
        byte[] initVectorBytes = getCryptoBytesFromMetadata(HttpHeaders.CRYPTO_IV.toString(), metadata);
        String materialsDescriptionString = getStringFromMetadata(HttpHeaders.MATERIALS_DESCRIPTION.toString(), metadata);
        Map<String, String> materialsDescription = convertJSONToMap(materialsDescriptionString);

        if (encryptedSymmetricKeyBytes == null || initVectorBytes == null) {
            // If necessary encryption info was not found in the instruction file, throw an exception.
            throw new Ks3ClientException(
                    String.format("Necessary encryption info not found in the headers of file '%s' in bucket '%s'",
                                  object.getKey(), object.getBucketName()));
        }

        EncryptionMaterials materials = retrieveOriginalMaterials(materialsDescription, materialsProvider);
        // If we're unable to retrieve the original encryption materials, we can't decrypt the object, so
        // throw an exception.
        if (materials == null) {
            throw new Ks3ClientException(
                    String.format("Unable to retrieve the encryption materials that originally " +
                            "encrypted file '%s' in bucket '%s'.",
                            object.getKey(), object.getBucketName()));
        }

        // Decrypt the symmetric key and create the symmetric cipher
        SecretKey symmetricKey = getDecryptedSymmetricKey(encryptedSymmetricKeyBytes, materials, cryptoProvider);
        CipherFactory cipherFactory = new CipherFactory(symmetricKey, Cipher.DECRYPT_MODE, initVectorBytes, cryptoProvider);

        return new EncryptionInstruction(materialsDescription, encryptedSymmetricKeyBytes, symmetricKey, cipherFactory);
    }

    /**
     * Returns an updated request where the input stream contains the encrypted object contents.
     * The specified instruction will be used to encrypt data.
     *
     * @param request
     *      The request whose contents are to be encrypted.
     * @param instruction
     *      The instruction that will be used to encrypt the object data.
     * @return
     *      The updated request where the input stream contains the encrypted contents.
     */
    public static PutObjectRequest encryptRequestUsingInstruction(PutObjectRequest request, EncryptionInstruction instruction) {
        // Create a new metadata object if there is no metadata already.
        ObjectMetadata metadata = request.getObjectMeta();
        if (metadata == null) {
            metadata = new ObjectMetadata();
        }

        // Record the original Content MD5, if present, for the unencrypted data
        if (metadata.getContentMD5() != null) {
            metadata.setUserMeta(HttpHeaders.UNENCRYPTED_CONTENT_MD5.toString(), metadata.getContentMD5());
        }
        
        // Removes the original content MD5 if present from the meta data.
        metadata.setContentMD5(null);
        
        // Record the original, unencrypted content-length so it can be accessed later
        final long plaintextLength = getUnencryptedContentLength(request, metadata);
        if (plaintextLength >= 0) {
            metadata.setUserMeta(HttpHeaders.UNENCRYPTED_CONTENT_LENGTH.toString(),
                Long.toString(plaintextLength));
        }

        // Put the calculated length of the encrypted contents in the metadata
        long cryptoContentLength = calculateCryptoContentLength(instruction.getSymmetricCipher(), request, metadata);
        if (cryptoContentLength >= 0) {
            metadata.setContentLength(cryptoContentLength);
        }

        request.setObjectMeta(metadata);

        // Create encrypted input stream
        request.setInputStream(getEncryptedInputStream(request, instruction.getCipherFactory(), plaintextLength));

        // Treat all encryption requests as input stream upload requests, not as file upload requests.
        request.setFile(null);

        return request;
    }

    /**
     * Returns an updated object where the object content input stream contains the decrypted contents.
     *
     * @param object
     *      The object whose contents are to be decrypted.
     * @param instruction
     *      The instruction that will be used to decrypt the object data.
     * @return
     *      The updated object where the object content input stream contains the decrypted contents.
     */
    public static Ks3Object decryptObjectUsingInstruction(Ks3Object object, EncryptionInstruction instruction) {
        AutoAbortInputStream objectContent = object.getObjectContent();

        InputStream decryptedInputStream = new RepeatableCipherInputStream(objectContent, instruction.getCipherFactory());
        object.setObjectContent(new AutoAbortInputStream(decryptedInputStream, objectContent.getRequest()));
        return object;
    }

    /**
     * Creates a put request to store the specified instruction object in S3.
     *
     * @param request
     *      The put request for the original object to be stored in S3.
     * @param instruction
     *      The instruction object to be stored in S3.
     * @return
     *      A put request to store the specified instruction object in S3.
     */
    public static PutObjectRequest createInstructionPutRequest(PutObjectRequest request, EncryptionInstruction instruction) {
        JSONObject instructionJSON = convertInstructionToJSONObject(instruction);
        byte[] instructionBytes = instructionJSON.toString().getBytes();
        InputStream instructionInputStream = new ByteArrayInputStream(instructionBytes);

        ObjectMetadata metadata = request.getObjectMeta();

        // Set the content-length of the upload
        metadata.setContentLength(instructionBytes.length);
        //不能使用request之前带着的content-md5
        metadata.setContentMD5(null);

        // Set the crypto instruction file header
        metadata.setUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString(),Base64.encodeAsString((request.getKey() + INSTRUCTION_SUFFIX).getBytes()));

        // Update the instruction request
        request.setKey(request.getKey() + INSTRUCTION_SUFFIX);
        request.setObjectMeta(metadata);
        request.setInputStream(instructionInputStream);
        return request;
    }

    public static PutObjectRequest createInstructionPutRequest(String bucketName, String key, EncryptionInstruction instruction) {
        JSONObject instructionJSON = convertInstructionToJSONObject(instruction);
        byte[] instructionBytes = instructionJSON.toString().getBytes();
        InputStream instructionInputStream = new ByteArrayInputStream(instructionBytes);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(instructionBytes.length);
        metadata.setUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString(),Base64.encodeAsString(( key + INSTRUCTION_SUFFIX).getBytes()));

        return new PutObjectRequest(bucketName, key + INSTRUCTION_SUFFIX, instructionInputStream, metadata);
    }

    /**
     * Creates a get request to retrieve an instruction file from S3.
     *
     * @param request
     *      The get request for the original object to be retrieved from S3.
     * @return
     *      A get request to retrieve an instruction file from S3.
     */
    public static GetObjectRequest createInstructionGetRequest(GetObjectRequest request) {
        return new GetObjectRequest(request.getBucket(), request.getKey() + INSTRUCTION_SUFFIX);
    }

    /**
     * Creates a delete request to delete an instruction file in S3.
     *
     * @param request
     *      The delete request for the original object to be deleted from S3.
     * @return
     *      A delete request to delete an instruction file in S3.
     */
    public static DeleteObjectRequest createInstructionDeleteObjectRequest(DeleteObjectRequest request) {
        return new DeleteObjectRequest(request.getBucket(), request.getKey() + INSTRUCTION_SUFFIX);
    }

    /**
     * Returns true if the specified S3Object contains encryption info in its
     * metadata, false otherwise.
     *
     * @param retrievedObject
     *      An S3Object
     * @return
     *      True if the specified S3Object contains encryption info in its
     *      metadata, false otherwise.
     */
    public static boolean isEncryptionInfoInMetadata(Ks3Object retrievedObject) {
    	if(retrievedObject.getObjectMetadata()==null)
    		retrievedObject.setObjectMetadata(new ObjectMetadata());
    	String iv = retrievedObject.getObjectMetadata().getUserMeta(HttpHeaders.CRYPTO_IV.toString());
    	String key = retrievedObject.getObjectMetadata().getUserMeta(HttpHeaders.CRYPTO_KEY.toString());
        return (!StringUtils.isBlank(iv))&&(!StringUtils.isBlank(key));
    }

    /**
     * Returns true if the specified S3Object is an instruction file containing
     * encryption info, false otherwise.
     *
     * @param instructionFile
     *      An S3Object that may potentially be an instruction file
     * @return
     *      True if the specified S3Object is an instruction file containing
     *      encryption info, false otherwise.
     */
    public static boolean isEncryptionInfoInInstructionFile(Ks3Object instructionFile) {
        if (instructionFile == null) {
            return false;
        }
        ObjectMetadata metadata = instructionFile.getObjectMetadata();
        if (metadata == null) {
            return false;
        }
        return metadata.containsUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString());
    }

    /**
     * Adjusts a user specified range to retrieve all of the cipher blocks (each of size 16 bytes) that
     * contain the specified range.
     *
     * For Chained Block Cipher decryption to function properly, we need to retrieve the cipher block that precedes
     * the range, all of the cipher blocks that contain the range, and the cipher block that follows the range.
     *
     * @param range
     *      A two-element array of longs corresponding to the start and finish (inclusive) of a desired
     *      range of bytes.
     * @return
     *      A two-element array of longs corresponding to the start and finish of the cipher blocks to
     *      be retrieved.  If the range is invalid, then return null.
     */
    public static long[] getAdjustedCryptoRange(long[] range) {
        // If range is invalid, then return null.
        if (range == null || range[0] > range[1]) {
            return null;
        }
        long[] adjustedCryptoRange = new long[2];
        adjustedCryptoRange[0] = getCipherBlockLowerBound(range[0]);
        adjustedCryptoRange[1] = getCipherBlockUpperBound(range[1]);
        return adjustedCryptoRange;
    }

    /**
     * Adjusts the retrieved S3Object so that the object contents contain only the range of bytes
     * desired by the user.  Since encrypted contents can only be retrieved in CIPHER_BLOCK_SIZE
     * (16 bytes) chunks, the S3Object potentially contains more bytes than desired, so this method
     * adjusts the contents range.
     *
     * @param object
     *      The S3Object retrieved from S3 that could possibly contain more bytes than desired
     *      by the user.
     * @param range
     *      A two-element array of longs corresponding to the start and finish (inclusive) of a desired
     *      range of bytes.
     * @return
     *      The S3Object with adjusted object contents containing only the range desired by the user.
     *      If the range specified is invalid, then the S3Object is returned without any modifications.
     */
    public static Ks3Object adjustOutputToDesiredRange(Ks3Object object, long[] range) {
        if (range == null || range[0] > range[1]) {
            // Make no modifications if range is invalid.
            return object;
        } else {
            try {
                AutoAbortInputStream objectContent = object.getObjectContent();
                InputStream adjustedRangeContents = new AdjustedRangeInputStream(objectContent, range[0], range[1]);
                object.setObjectContent(new AutoAbortInputStream(adjustedRangeContents, objectContent.getRequest()));
                return object;
            } catch (IOException e) {
                throw new Ks3ClientException("Error adjusting output to desired byte range: " + e.getMessage());
            }
        }
    }

    /**
     * Generates a one-time use Symmetric Key on-the-fly for use in envelope encryption.
     */
    public static SecretKey generateOneTimeUseSymmetricKey() {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance(JceEncryptionConstants.SYMMETRIC_KEY_ALGORITHM);
            generator.init(JceEncryptionConstants.SYMMETRIC_KEY_LENGTH, new SecureRandom());
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new Ks3ClientException("Unable to generate envelope symmetric key:" + e.getMessage(), e);
        }
    }

    /**
     * Creates a symmetric cipher in the specified mode from the given symmetric key and IV.  The given
     * crypto provider will provide the encryption implementation.  If the crypto provider is null, then
     * the default JCE crypto provider will be used.
     */
    public static Cipher createSymmetricCipher(SecretKey symmetricCryptoKey, int encryptMode, Provider cryptoProvider, byte[] initVector) {
        try {
            Cipher cipher;
            if (cryptoProvider != null) {
                cipher = Cipher.getInstance(JceEncryptionConstants.SYMMETRIC_CIPHER_METHOD, cryptoProvider);
            } else {
                cipher = Cipher.getInstance(JceEncryptionConstants.SYMMETRIC_CIPHER_METHOD);
            }
            if (initVector != null) {
                cipher.init(encryptMode, symmetricCryptoKey, new IvParameterSpec(initVector));
            } else {
                cipher.init(encryptMode, symmetricCryptoKey);
            }
            return cipher;
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to build cipher: " + e.getMessage() +
                    "\nMake sure you have the JCE unlimited strength policy files installed and " +
                    "configured for your JVM: http://www.ngs.ac.uk/tools/jcepolicyfiles", e);
        }
    }

    /**
     * Encrypts a symmetric key using the provided encryption materials and returns
     * it in raw byte array form.
     */
    public static byte[] getEncryptedSymmetricKey(SecretKey toBeEncrypted, EncryptionMaterials materials, Provider cryptoProvider) {
        Key keyToDoEncryption;
        if (materials.getKeyPair() != null) {
            // Do envelope encryption with public key from key pair
            keyToDoEncryption = materials.getKeyPair().getPublic();
        } else {
            // Do envelope encryption with symmetric key
            keyToDoEncryption= materials.getSymmetricKey();
        }
        try {
            Cipher cipher;
            byte[] toBeEncryptedBytes = toBeEncrypted.getEncoded();
            if (cryptoProvider != null) {
                cipher = Cipher.getInstance(keyToDoEncryption.getAlgorithm(), cryptoProvider);
            } else {
                cipher = Cipher.getInstance(keyToDoEncryption.getAlgorithm()); // Use default JCE Provider
            }
            cipher.init(Cipher.ENCRYPT_MODE, keyToDoEncryption);
            return cipher.doFinal(toBeEncryptedBytes);
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to encrypt symmetric key: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts an encrypted symmetric key using the provided encryption materials and returns
     * it as a SecretKey object.
     */
    private static SecretKey getDecryptedSymmetricKey(byte[] encryptedSymmetricKeyBytes, EncryptionMaterials materials, Provider cryptoProvider) {
        Key keyToDoDecryption;
        if (materials.getKeyPair() != null) {
            // Do envelope decryption with private key from key pair
            keyToDoDecryption = materials.getKeyPair().getPrivate();
        } else {
            // Do envelope decryption with symmetric key
            keyToDoDecryption = materials.getSymmetricKey();
        }
        try {
            Cipher cipher;
            if (cryptoProvider != null) {
                cipher = Cipher.getInstance(keyToDoDecryption.getAlgorithm(), cryptoProvider);
            } else {
                cipher = Cipher.getInstance(keyToDoDecryption.getAlgorithm());
            }
            cipher.init(Cipher.DECRYPT_MODE, keyToDoDecryption);
            byte[] decryptedSymmetricKeyBytes = cipher.doFinal(encryptedSymmetricKeyBytes);
            return new SecretKeySpec(decryptedSymmetricKeyBytes, JceEncryptionConstants.SYMMETRIC_KEY_ALGORITHM);
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to decrypt symmetric key from object metadata : " + e.getMessage(), e);
        }
    }

    /**
     * @param plaintextLength
     *            the expected total number of bytes of the plaintext; or -1 if
     *            not available.
     */
    private static InputStream getEncryptedInputStream(
            PutObjectRequest request, CipherFactory cipherFactory,
            long plaintextLength) {
        try {
            InputStream is = request.getInputStream();
            if (request.getFile() != null) {
                // Historically file takes precedence over the original input
                // stream
                is = new RepeatableFileInputStream(request.getFile());
            }
            //这个和put object request中的并不冲突，put object中指定的是加密后的长度，因为put object request中的inputstream
            //是对 RepeatableCipherInputStream的封装，而这里的是还没有加密的
            if (plaintextLength > -1) {
                // This ensures the plain-text read from the underlying data
                // stream has the same length as the expected total
                is = new LengthCheckInputStream(is, plaintextLength,
                		LengthCheckInputStream.EXCLUDE_SKIPPED_BYTES);
            }
            return new RepeatableCipherInputStream(is, cipherFactory);
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to create cipher input stream: " + e.getMessage(), e);
        }
    }

    public static ByteRangeCapturingInputStream getEncryptedInputStream(UploadPartRequest request, CipherFactory cipherFactory) {
        try {
            InputStream originalInputStream = request.getInputStream();
            if (request.getFile() != null) {
                originalInputStream = new InputSubStream(new RepeatableFileInputStream(request.getFile()),
                        request.getFileoffset(), request.getInstancePartSize(), request.isLastPart());
            }

            originalInputStream = new RepeatableCipherInputStream(originalInputStream, cipherFactory);

            if (request.isLastPart() == false) {
                // We want to prevent the final padding from being sent on the stream...
                originalInputStream = new InputSubStream(originalInputStream, 0, request.getInstancePartSize(), false);
            }

            long partSize = request.getInstancePartSize();
            int cipherBlockSize = cipherFactory.createCipher().getBlockSize();
            return new ByteRangeCapturingInputStream(originalInputStream, partSize - cipherBlockSize, partSize);
        } catch (Exception e) {
            throw new Ks3ClientException("Unable to create cipher input stream: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the byte[] value of either the crypto key or crypto IV.  If these fields are not found in
     * the metadata, returns null.
     *
     * Note: The bytes are transported in Base64-encoding, so they are decoded before they are returned.
     */
    private static byte[] getCryptoBytesFromMetadata(String headerName, ObjectMetadata metadata) throws NullPointerException {
        if (metadata == null || !metadata.containsUserMeta(headerName)) {
            return null;
        } else {
            // Convert Base64 bytes to binary data.
            return Base64.decode(metadata.getUserMeta(headerName));
        }
    }

    /**
     * Retrieves the String value of the given header from the metadata.  Returns null if the field is not
     * found in the metadata.
     */
    private static String getStringFromMetadata(String headerName, ObjectMetadata metadata) throws NullPointerException {
        if (metadata == null || !metadata.containsUserMeta(headerName)) {
            return null;
        } else {
            return metadata.getUserMeta(headerName);
        }
    }

    /**
     * Converts the JSON encoded materials description to a Map<String, String>
     */
    @SuppressWarnings("unchecked") // Suppresses Iterator<String> type warning
    private static Map<String, String> convertJSONToMap(String descriptionJSONString) {
        if (descriptionJSONString == null) {
            return null;
        }
            JSONObject descriptionJSON = new JSONObject(descriptionJSONString);
            Iterator<String> keysIterator = descriptionJSON.keys();
            Map<String, String> materialsDescription = new HashMap<String, String>();
            while(keysIterator.hasNext()) {
                String key = keysIterator.next();
                materialsDescription.put(key, descriptionJSON.getString(key));
            }
            return materialsDescription;
    }

    /**
     * Update the request's ObjectMetadata with the necessary information for decrypting the object
     *
     * @param request
     *      Non-null PUT request encrypted using the given instruction
     * @param instruction
     *      Non-null instruction used to encrypt the data in this PUT request.
     */
    public static void updateMetadataWithEncryptionInstruction(PutObjectRequest request, EncryptionInstruction instruction){
        byte[] keyBytesToStoreInMetadata = instruction.getEncryptedSymmetricKey();
        Cipher symmetricCipher = instruction.getSymmetricCipher();
        Map<String, String> materialsDescription = instruction.getMaterialsDescription();

        ObjectMetadata metadata = request.getObjectMeta();
        if (metadata == null) metadata = new ObjectMetadata();

        if (request.getFile() != null) {
            Mimetypes mimetypes = Mimetypes.getInstance();
            metadata.setContentType(mimetypes.getMimetype(request.getFile()));
        }

        updateMetadata(metadata, keyBytesToStoreInMetadata, symmetricCipher, materialsDescription);
        request.setObjectMeta( metadata );
    }

    private static void updateMetadata(ObjectMetadata metadata, byte[] keyBytesToStoreInMetadata, Cipher symmetricCipher, Map<String, String> materialsDescription) {
        // If we generated a symmetric key to encrypt the data, store it in the object metadata.
        if (keyBytesToStoreInMetadata != null) {
            metadata.setUserMeta(HttpHeaders.CRYPTO_KEY.toString(),
                    Base64.encodeAsString(keyBytesToStoreInMetadata));
        }

        // Put the cipher initialization vector (IV) into the object metadata
        metadata.setUserMeta(HttpHeaders.CRYPTO_IV.toString(),
                Base64.encodeAsString(symmetricCipher.getIV()));

        // Put the materials description into the object metadata as JSON
        JSONObject descriptionJSON = new JSONObject(materialsDescription);
        metadata.setUserMeta(HttpHeaders.MATERIALS_DESCRIPTION.toString(), descriptionJSON.toString());
    }

    public static ObjectMetadata updateMetadataWithEncryptionInfo(InitiateMultipartUploadRequest request, byte[] keyBytesToStoreInMetadata, Cipher symmetricCipher, Map<String, String> materialsDescription) {
        ObjectMetadata metadata = request.getObjectMeta();
        if (metadata == null) metadata = new ObjectMetadata();

        updateMetadata(metadata, keyBytesToStoreInMetadata, symmetricCipher, materialsDescription);

        return metadata;
    }

    /**
     * Retrieve the original materials corresponding to the specified materials description.
     * Returns null if unable to retrieve the original materials.
     */
    private static EncryptionMaterials retrieveOriginalMaterials(Map<String, String> materialsDescription, EncryptionMaterialsAccessor accessor) {
        if (accessor == null)
            return null;
        return accessor.getEncryptionMaterials(materialsDescription);
    }

    /**
     * Calculates the length of the encrypted file given the original plaintext
     * file length and the cipher that will be used for encryption.
     *
     * @return
     *      The size of the encrypted file in bytes, or -1 if no content length
     *      has been set yet.
     */
    private static long calculateCryptoContentLength(Cipher symmetricCipher, PutObjectRequest request, ObjectMetadata metadata) {
        long plaintextLength = getUnencryptedContentLength(request, metadata);

        // If we don't know the unencrypted size, then report -1
        if (plaintextLength < 0) return -1;

        long cipherBlockSize = symmetricCipher.getBlockSize();
        long offset = cipherBlockSize - (plaintextLength % cipherBlockSize);
        return plaintextLength + offset;
    }

    public static long calculateCryptoContentLength(Cipher symmetricCipher, UploadPartRequest request) {
        long plaintextLength;
        if (request.getFile() != null) {
            if (request.getInstancePartSize() > 0) plaintextLength = request.getInstancePartSize();
            else plaintextLength = request.getFile().length();
        } else if (request.getInputStream() != null) {
            plaintextLength = request.getInstancePartSize();
        } else {
            return -1;
        }
        long cipherBlockSize = symmetricCipher.getBlockSize();
        long offset = cipherBlockSize - (plaintextLength % cipherBlockSize);
        return plaintextLength + offset;
    }

    /**
     * Returns the content length of the unencrypted data in a PutObjectRequest,
     * or -1 if the original content-length isn't known.
     *
     * @param request
     *            The request to examine.
     * @param metadata
     *            The metadata for the request.
     *
     * @return The content length of the unencrypted data in the request, or -1
     *         if it isn't known.
     */
    private static long getUnencryptedContentLength(PutObjectRequest request, ObjectMetadata metadata) {
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

    /**
     * Returns a JSONObject representation of the instruction object.
     */
    private static JSONObject convertInstructionToJSONObject(EncryptionInstruction instruction) {
        JSONObject instructionJSON = new JSONObject();
            instructionJSON.put(HttpHeaders.MATERIALS_DESCRIPTION.toString(),
            		//要先转化成String再put的原因详见ContentCryptoMaterial 314行
            		Jackson.toJsonString(instruction.getMaterialsDescription()));
            instructionJSON.put(HttpHeaders.CRYPTO_KEY.toString(), 
                Base64.encodeAsString(instruction.getEncryptedSymmetricKey()));
            byte[] iv = instruction.getSymmetricCipher().getIV();
            instructionJSON.put(HttpHeaders.CRYPTO_IV.toString(), Base64.encodeAsString(iv));
        return instructionJSON;
    }

    /**
     * Parses instruction data retrieved from S3 and returns a JSONObject representing the instruction
     */
    private static JSONObject parseJSONInstruction(Ks3Object instructionObject) {
        try {
            String instructionString = convertStreamToString(instructionObject.getObjectContent());
            return new JSONObject(instructionString);
        } catch (Exception e) {
            throw new Ks3ClientException("Error parsing JSON instruction file: " + e.getMessage());
        }
    }

    /**
     * Converts the contents of an input stream to a String
     */
    private static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }else {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } finally {
                inputStream.close();
            }
            return stringBuilder.toString();
        }
    }

    /**
     * Takes the position of the leftmost desired byte of a user specified range and returns the
     * position of the start of the previous cipher block, or returns 0 if the leftmost byte is in
     * the first cipher block.
     */
    private static long getCipherBlockLowerBound(long leftmostBytePosition) {
        long cipherBlockSize = JceEncryptionConstants.SYMMETRIC_CIPHER_BLOCK_SIZE;
        long offset = leftmostBytePosition % cipherBlockSize;
        long lowerBound = leftmostBytePosition - offset - cipherBlockSize;
        if (lowerBound < 0) {
            return 0;
        } else {
            return lowerBound;
        }
    }

    /**
     * Takes the position of the rightmost desired byte of a user specified range and returns the
     * position of the end of the following cipher block.
     */
    private static long getCipherBlockUpperBound(long rightmostBytePosition) {
        long cipherBlockSize = JceEncryptionConstants.SYMMETRIC_CIPHER_BLOCK_SIZE;
        long offset = cipherBlockSize - (rightmostBytePosition % cipherBlockSize);
        return rightmostBytePosition + offset + cipherBlockSize;
    }
}
