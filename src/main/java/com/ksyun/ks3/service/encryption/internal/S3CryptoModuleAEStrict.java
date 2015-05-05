
package com.ksyun.ks3.service.encryption.internal;

import com.ksyun.ks3.service.encryption.S3Direct;
import com.ksyun.ks3.service.encryption.model.CryptoConfiguration;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;


/**
 * Strict Authenticated encryption (AE) cryptographic module for the S3
 * encryption client.
 */
class S3CryptoModuleAEStrict extends S3CryptoModuleAE {
    S3CryptoModuleAEStrict(S3Direct s3,
            EncryptionMaterialsProvider encryptionMaterialsProvider,
            CryptoConfiguration cryptoConfig) {
        super(s3, encryptionMaterialsProvider
                , cryptoConfig);
    }


    protected final boolean isStrict() {
        return true;
    }

    protected void securityCheck(ContentCryptoMaterial cekMaterial,
            S3ObjectWrapper retrieved) {
        if (!ContentCryptoScheme.AES_GCM.equals(cekMaterial.getContentCryptoScheme())) {
            throw new SecurityException("S3 object [bucket: "
                    + retrieved.getBucketName() + ", key: "
                    + retrieved.getKey()
                    + "] not encrypted using authenticated encryption");
        }
    }
}
