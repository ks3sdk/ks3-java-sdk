
package com.ksyun.ks3.service.encryption.internal;


final class MultipartUploadCryptoContext extends MultipartUploadContext {
    private final ContentCryptoMaterial cekMaterial;

    MultipartUploadCryptoContext(String bucketName, String key,
            ContentCryptoMaterial cekMaterial) {
        super(bucketName, key);
        this.cekMaterial = cekMaterial;
    }

    /**
     * Convenient method to return the content encrypting cipher lite (which is
     * stateful) for the multi-part uploads.
     */
    CipherLite getCipherLite() {
        return cekMaterial.getCipherLite();
    }

    /**
     * Returns the content encrypting cryptographic material for the multi-part
     * uploads.
     */
    ContentCryptoMaterial getContentCryptoMaterial() {
        return cekMaterial;
    }
}
