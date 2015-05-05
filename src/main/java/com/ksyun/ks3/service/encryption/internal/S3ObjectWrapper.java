
package com.ksyun.ks3.service.encryption.internal;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * A convenient S3 object wrapper useful for crypto purposes.
 */
class S3ObjectWrapper implements Closeable {
    private final Ks3Object s3obj;
    
    S3ObjectWrapper(Ks3Object s3obj) {
        if (s3obj == null)
            throw new IllegalArgumentException();
        this.s3obj = s3obj;
    }

    ObjectMetadata getObjectMetadata() {
        return s3obj.getObjectMetadata();
    }

    void setObjectMetadata(ObjectMetadata metadata) {
        s3obj.setObjectMetadata(metadata);
    }

    AutoAbortInputStream getObjectContent() {
        return s3obj.getObjectContent();
    }

    void setObjectContent(AutoAbortInputStream objectContent) {
        s3obj.setObjectContent(objectContent);
    }
    void setObjectContent(InputStream objectContent) {
        s3obj.setObjectContent(objectContent);
    }
    

    String getBucketName() {
        return s3obj.getBucketName();
    }

    void setBucketName(String bucketName) {
        s3obj.setBucketName(bucketName);
    }

    String getKey() {
        return s3obj.getKey();
    }

    void setKey(String key) {
        s3obj.setKey(key);
    }
    
    String getRedirectLocation() {
        return s3obj.getRedirectLocation();
    }
    
    void setRedirectLocation(String redirectLocation) {
        s3obj.setRedirectLocation(redirectLocation);
    }

    @Override public String toString() {
        return s3obj.toString();
    }

    /**
     * Returns true if this S3 object is an instruction file; false otherwise.
     */
    final boolean isInstructionFile() {
        ObjectMetadata metadata = s3obj.getObjectMetadata();
        return metadata != null
            && metadata.containsUserMeta(HttpHeaders.CRYPTO_INSTRUCTION_FILE.toString());
    }

    /**
     * Returns true if this S3 object has the encryption information stored
     * as user meta data; false otherwise.
     */
    final boolean hasEncryptionInfo() {
        ObjectMetadata metadata = s3obj.getObjectMetadata();
        return metadata != null
            && metadata.containsUserMeta(HttpHeaders.CRYPTO_IV.toString())
            && (metadata.containsUserMeta(HttpHeaders.CRYPTO_KEY_V2.toString())
                || metadata.containsUserMeta(HttpHeaders.CRYPTO_KEY.toString()));
    }

    /**
     * Converts and return the underlying S3 object as a json string.
     * 
     * @throws KClientException if failed in JSON conversion.
     */
    String toJsonString() {
        try {
            return from(s3obj.getObjectContent());
        } catch (Exception e) {
            throw new Ks3ClientException("Error parsing JSON: " + e.getMessage());
        }
    }

    private static String from(InputStream is) throws IOException {
        if (is == null)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } finally {
            is.close();
        }
        return stringBuilder.toString();
    }

    public void close() throws IOException {
        s3obj.close();
    }

    Ks3Object getS3Object() { return s3obj; }

    /**
     * Returns the original crypto scheme used for encryption, which may
     * differ from the crypto scheme used for decryption during, for example,
     * a range-get operation. 
     * 
     * @param instructionFile
     *            the instruction file of the s3 object; or null if there is
     *            none.
     */
    ContentCryptoScheme encryptionSchemeOf(Map<String,String> instructionFile) {
        if (instructionFile != null) {
            String cekAlgo = instructionFile.get(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString());
            return ContentCryptoScheme.fromCEKAlgo(cekAlgo);
        }
        ObjectMetadata meta = s3obj.getObjectMetadata();
        String cekAlgo = meta.getUserMeta(HttpHeaders.CRYPTO_CEK_ALGORITHM.toString());
        return ContentCryptoScheme.fromCEKAlgo(cekAlgo);
    }
}
