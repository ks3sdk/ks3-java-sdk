
package com.ksyun.ks3.service.encryption.internal;

import java.security.Key;

final class S3KeyWrapScheme {
    public static final String AESWrap = "AESWrap"; 
    public static final String RSA_ECB_OAEPWithSHA256AndMGF1Padding = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    String getKeyWrapAlgorithm(Key key) {
        String algorithm = key.getAlgorithm();
        if (S3CryptoScheme.AES.equals(algorithm)) {
            return AESWrap;
        }
        if (S3CryptoScheme.RSA.equals(algorithm)) {
            if (CryptoRuntime.isRsaKeyWrapAvailable())
                return RSA_ECB_OAEPWithSHA256AndMGF1Padding;
        }
        return null;
    }
}