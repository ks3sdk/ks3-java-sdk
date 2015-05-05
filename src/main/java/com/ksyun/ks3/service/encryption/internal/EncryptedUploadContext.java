
package com.ksyun.ks3.service.encryption.internal;

import javax.crypto.SecretKey;

/**
 * State information for an in-progress, encrypted multipart upload, including
 * the envelope encryption key used to encrypt each individual part in a
 * multipart upload, and the next initialization vector (IV) for the next part
 * to encrypt.
 */
public class EncryptedUploadContext extends MultipartUploadContext {
    private final SecretKey envelopeEncryptionKey;
    private byte[] firstIV;
    private byte[] nextIV;

    public EncryptedUploadContext(String bucketName, String key,
            SecretKey envelopeEncryptionKey) {
        super(bucketName, key);
        this.envelopeEncryptionKey = envelopeEncryptionKey;
    }

    public SecretKey getEnvelopeEncryptionKey() {
        return envelopeEncryptionKey;
    }

    public void setNextInitializationVector(byte[] nextIV) {
        this.nextIV = nextIV;
    }

    public byte[] getNextInitializationVector() {
        return nextIV;
    }

    public void setFirstInitializationVector(byte[] firstIV) {
        this.firstIV = firstIV;
    }

    public byte[] getFirstInitializationVector() {
        return firstIV;
    }
}
