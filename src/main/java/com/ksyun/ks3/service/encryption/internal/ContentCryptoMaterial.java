package com.ksyun.ks3.service.encryption.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;







import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsAccessor;
import com.ksyun.ks3.utils.*;


/**
 * Cryptographic material used for client-side content encrypt/decryption in S3.
 * This includes the randomly generated one-time secured CEK
 * (content-encryption-key) and the respective key wrapping algorithm, if any,
 * and the cryptographic scheme in use.
 */
final class ContentCryptoMaterial {
    // null if cek is not secured via key wrapping
    private final String keyWrappingAlgorithm; 
    private final CipherLite cipherLite;

    private final Map<String, String> kekMaterialsDescription;
    private final byte[] encryptedCEK;

    ContentCryptoMaterial(Map<String, String> kekMaterialsDescription,
            byte[] encryptedCEK,
            String keyWrappingAlgorithm,
            CipherLite cipherLite) {
        this.cipherLite = cipherLite;
        this.keyWrappingAlgorithm = keyWrappingAlgorithm;
        this.encryptedCEK = encryptedCEK.clone();
        this.kekMaterialsDescription = kekMaterialsDescription;
    }

    /**
     * Returns the key wrapping algorithm, or null if the content key is not
     * secured via a key wrapping algorithm.
     */
    String getKeyWrappingAlgorithm() {
        return keyWrappingAlgorithm;
    }

    /**
     * Returns the content crypto scheme.
     */
    ContentCryptoScheme getContentCryptoScheme() {
        return cipherLite.getContentCryptoScheme();
    }

    /**
     * Returns the given metadata updated with this content crypto material.
     */
    ObjectMetadata toObjectMetadata(ObjectMetadata metadata) {
        // If we generated a symmetric key to encrypt the data, store it in the
        // object metadata.
        byte[] encryptedCEK = getEncryptedCEK();
        metadata.setUserMeta(HttpHeaders.CRYPTO_KEY_V2.toString(),
                Base64.encodeAsString(encryptedCEK));
        // Put the cipher initialization vector (IV) into the object metadata
        byte[] iv = cipherLite.getIV();
        metadata.setUserMeta(HttpHeaders.CRYPTO_IV.toString(), Base64.encodeAsString(iv));
        // Put the materials description into the object metadata as JSON
        metadata.setUserMeta(HttpHeaders.MATERIALS_DESCRIPTION.toString(),
                kekMaterialDescAsJson());
        // The CRYPTO_CEK_ALGORITHM, CRYPTO_TAG_LENGTH and
        // CRYPTO_KEYWRAP_ALGORITHM were not available in the Encryption Only
        // (EO) implementation
        ContentCryptoScheme scheme = getContentCryptoScheme();
        metadata.setUserMeta(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString(),
                scheme.getCipherAlgorithm());
        int tagLen = scheme.getTagLengthInBits();
        if (tagLen > 0)
            metadata.setUserMeta(HttpHeaders.CRYPTO_TAG_LENGTH.toString(),
                    String.valueOf(tagLen));
        String keyWrapAlgo = getKeyWrappingAlgorithm();
        if (keyWrapAlgo != null)
            metadata.setUserMeta(HttpHeaders.CRYPTO_KEYWRAP_ALGORITHM.toString(),
                    keyWrapAlgo);
        return metadata;
    }


    String toJsonString() {
        Map<String, String> map = new HashMap<String, String>();
        byte[] encryptedCEK = getEncryptedCEK();
        map.put(HttpHeaders.CRYPTO_KEY_V2.toString(), Base64.encodeAsString(encryptedCEK));
        byte[] iv = cipherLite.getIV();
        map.put(HttpHeaders.CRYPTO_IV.toString(), Base64.encodeAsString(iv));
        map.put(HttpHeaders.MATERIALS_DESCRIPTION.toString(), kekMaterialDescAsJson());
        // The CRYPTO_CEK_ALGORITHM, CRYPTO_TAG_LENGTH and
        // CRYPTO_KEYWRAP_ALGORITHM were not available in the Encryption Only
        // (EO) implementation
        ContentCryptoScheme scheme = getContentCryptoScheme();
        map.put(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString(), scheme.getCipherAlgorithm());
        int tagLen = scheme.getTagLengthInBits();
        if (tagLen > 0)
            map.put(HttpHeaders.CRYPTO_TAG_LENGTH.toString(), String.valueOf(tagLen));
        String keyWrapAlgo = getKeyWrappingAlgorithm();
        if (keyWrapAlgo != null)
            map.put(HttpHeaders.CRYPTO_KEYWRAP_ALGORITHM.toString(), keyWrapAlgo);
        return Jackson.toJsonString(map);
    }
    
    /**
     * Returns the key-encrypting-key material description as a non-null json
     * string;
     */
    private String kekMaterialDescAsJson() {
        Map<String,String> kekMaterialDesc = getKEKMaterialsDescription();
        if (kekMaterialDesc == null)
            kekMaterialDesc = Collections.emptyMap();
        return Jackson.toJsonString(kekMaterialDesc);
    }

    /**
     * Returns the corresponding kek material description from the given json;
     * or null if the input is null.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> matdescFromJson(String json) {
        Map<String,String> map = Jackson.fromJsonString(json, Map.class);
        return map == null ? null : Collections.unmodifiableMap(map);
    }

    /**
     * Returns the content encrypting key unwrapped or decrypted.
     * 
     * @param cekSecured
     *            the content encrypting key in wrapped or encrypted form; must
     *            not be null
     * @param keyWrapAlgo
     *            key wrapping algorithm; or null if direct encryption instead
     *            of key wrapping is used
     * @param materials
     *            the client key encrypting key material for the content
     *            encrypting key
     * @param securityProvider
     *            security provider or null if the default security provider of
     *            the JCE is used
     */
    private static SecretKey cek(byte[] cekSecured, String keyWrapAlgo,
            EncryptionMaterials materials, Provider securityProvider) {
        Key kek;
        if (materials.getKeyPair() != null) {
            // Do envelope decryption with private key from key pair
            kek = materials.getKeyPair().getPrivate();
        } else {
            // Do envelope decryption with symmetric key
            kek = materials.getSymmetricKey();
        }

        try {
            if (keyWrapAlgo != null) {
                // Key wrapping specified
                Cipher cipher = securityProvider == null ? Cipher
                        .getInstance(keyWrapAlgo) : Cipher.getInstance(
                        keyWrapAlgo, securityProvider);
                cipher.init(Cipher.UNWRAP_MODE, kek);
                return (SecretKey) cipher.unwrap(cekSecured, keyWrapAlgo,
                        Cipher.SECRET_KEY);
            }
            // fall back to the Encryption Only (EO) key decrypting method
            Cipher cipher;
            if (securityProvider != null) {
                cipher = Cipher.getInstance(kek.getAlgorithm(),
                        securityProvider);
            } else {
                cipher = Cipher.getInstance(kek.getAlgorithm());
            }
            cipher.init(Cipher.DECRYPT_MODE, kek);
            byte[] decryptedSymmetricKeyBytes = cipher.doFinal(cekSecured);
            return new SecretKeySpec(decryptedSymmetricKeyBytes,
                    JceEncryptionConstants.SYMMETRIC_KEY_ALGORITHM);
        } catch (Exception e) {
            throw new Ks3ClientException(
                    "Unable to decrypt symmetric key from object metadata : "
                            + e.getMessage(), e);
        }
    }

    /**
     * Factory method to return the content crypto material from the S3 object
     * meta data, using the specified key encrypting key material accessor and
     * an optional security provider.
     */
    static ContentCryptoMaterial fromObjectMetadata(
            ObjectMetadata metadata,
            EncryptionMaterialsAccessor kekMaterialAccessor,
            Provider securityProvider,
            long[] range) {
        // CEK and IV
        String b64key = metadata.getUserMeta(HttpHeaders.CRYPTO_KEY_V2.toString());
        if (b64key == null) {
            b64key = metadata.getUserMeta(HttpHeaders.CRYPTO_KEY.toString());
            if (b64key == null)
                throw new Ks3ClientException("Content encrypting key not found.");
        }
        byte[] cekWrapped = Base64.decode(b64key);
        byte[] iv = Base64.decode(metadata.getUserMeta(HttpHeaders.CRYPTO_IV.toString()));
        if (cekWrapped == null || iv == null) {
            throw new Ks3ClientException("Content encrypting key or IV not found.");
        }
        // Material description
        String matdescStr = metadata.getUserMeta(HttpHeaders.MATERIALS_DESCRIPTION.toString());
        Map<String, String> matdesc = matdescFromJson(matdescStr);
        EncryptionMaterials materials = kekMaterialAccessor == null
            ? null
            : kekMaterialAccessor.getEncryptionMaterials(matdesc)
            ;
        if (materials == null) {
            throw new Ks3ClientException(
                    "Unable to retrieve the client encryption materials");
        }
        // CEK algorithm
        String cekAlgo = metadata.getUserMeta(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString());
        boolean isRangeGet = range != null;
        // The content crypto scheme may vary depending on whether
        // it is a range get operation
        ContentCryptoScheme contentCryptoScheme = ContentCryptoScheme
                .fromCEKAlgo(cekAlgo, isRangeGet);
        if (isRangeGet) {
            // Adjust the IV as needed
            iv = contentCryptoScheme.adjustIV(iv, range[0]);
        } else {
            // Validate the tag length supported
            int tagLenExpected = contentCryptoScheme.getTagLengthInBits();
            if (tagLenExpected > 0) {
                String s = metadata.getUserMeta(HttpHeaders.CRYPTO_TAG_LENGTH.toString());
                int tagLenActual = Integer.parseInt(s);
                if (tagLenExpected != tagLenActual) {
                    throw new Ks3ClientException("Unsupported tag length: "
                            + tagLenActual + ", expected: " + tagLenExpected);
                }
            }
        }
        // Unwrap or decrypt the CEK
        String keyWrapAlgo = metadata.getUserMeta(HttpHeaders.CRYPTO_KEYWRAP_ALGORITHM.toString());
        SecretKey cek = cek(cekWrapped, keyWrapAlgo, materials,
                securityProvider);
        return new ContentCryptoMaterial(matdesc, cekWrapped, keyWrapAlgo,
                contentCryptoScheme.createCipherLite(cek, iv,
                        Cipher.DECRYPT_MODE, securityProvider));
    }

    /**
     * Factory method to return the content crypto material from the S3
     * instruction file, using the specified key encrypting key material
     * accessor and an optional security provider.
     */
    static ContentCryptoMaterial fromInstructionFile(Map<String,String> instFile,
        EncryptionMaterialsAccessor kekMaterialAccessor,
        Provider securityProvider,
        long[] range) {
        return fromInstructionFile0(instFile, kekMaterialAccessor,
                securityProvider, range);
    }

    private static ContentCryptoMaterial fromInstructionFile0(
            Map<String,String> map,
            EncryptionMaterialsAccessor kekMaterialAccessor,
            Provider securityProvider,
            long[] range) {
        // CEK and IV
        String b64key = map.get(HttpHeaders.CRYPTO_KEY_V2.toString());
        if (b64key == null) {
            b64key = map.get(HttpHeaders.CRYPTO_KEY.toString());
            if (b64key == null)
                throw new Ks3ClientException("Content encrypting key not found.");
        }
        byte[] cekWrapped = Base64.decode(b64key);
        byte[] iv = Base64.decode(map.get(HttpHeaders.CRYPTO_IV.toString()));
        if (cekWrapped == null || iv == null) {
            throw new Ks3ClientException(
                    "Necessary encryption info not found in the instruction file "
                            + map);
        }
        // Material description
        String matdescStr = map.get(HttpHeaders.MATERIALS_DESCRIPTION.toString());
        Map<String, String> matdesc = matdescFromJson(matdescStr);
        EncryptionMaterials materials = kekMaterialAccessor == null
            ? null
            : kekMaterialAccessor.getEncryptionMaterials(matdesc)
            ;
        if (materials == null) {
            throw new Ks3ClientException(
                    "Unable to retrieve the encryption materials that originally "
                            + "encrypted object corresponding to instruction file " + map);
        }
        // CEK algorithm
        String cekAlgo = map.get(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString());
        boolean isRangeGet = range != null;
        // The content crypto scheme may vary depending on whether
        // it is a range get operation
        ContentCryptoScheme contentCryptoScheme = ContentCryptoScheme
                .fromCEKAlgo(cekAlgo, isRangeGet);
        if (isRangeGet) {
            // Adjust the IV as needed
            iv = contentCryptoScheme.adjustIV(iv, range[0]);
        } else {
            // Validate the tag length supported
            int tagLenExpected = contentCryptoScheme.getTagLengthInBits();
            if (tagLenExpected > 0) {
                String s = map.get(HttpHeaders.CRYPTO_TAG_LENGTH.toString());
                int tagLenActual = Integer.parseInt(s);
                if (tagLenExpected != tagLenActual) {
                    throw new Ks3ClientException("Unsupported tag length: "
                            + tagLenActual + ", expected: " + tagLenExpected);
                }
            }
        }
        // Unwrap or decrypt the CEK
        String keyWrapAlgo = map.get(HttpHeaders.CRYPTO_KEYWRAP_ALGORITHM.toString());
        SecretKey cek = cek(cekWrapped, keyWrapAlgo, materials, securityProvider);
        return new ContentCryptoMaterial(matdesc, cekWrapped, keyWrapAlgo,
                contentCryptoScheme.createCipherLite(cek, iv,
                        Cipher.DECRYPT_MODE, securityProvider));
    }

    /**
     * Parses instruction data retrieved from S3 and returns a JSON string
     * representing the instruction. Made for testing purposes.
     */
    static String parseInstructionFile(Ks3Object instructionFile) {
        try {
            return convertStreamToString(instructionFile.getObjectContent());
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
     * Return the cipher lite used for content encryption/decryption purposes.
     */
    CipherLite getCipherLite() {
        return cipherLite;
    }

    /**
     * Returns the description of the kek materials that were used to
     * encrypt the cek.
     */
    Map<String, String> getKEKMaterialsDescription() {
        return this.kekMaterialsDescription;
    }

    /**
     * Returns an array of bytes representing the encrypted envelope symmetric
     * key.
     * 
     * @return an array of bytes representing the encrypted envelope symmetric
     *         key.
     */
    byte[] getEncryptedCEK() {
        return this.encryptedCEK.clone();
    }
}
