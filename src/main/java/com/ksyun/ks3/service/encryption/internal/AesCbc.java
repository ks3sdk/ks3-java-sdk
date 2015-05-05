package com.ksyun.ks3.service.encryption.internal;

class AesCbc extends ContentCryptoScheme {
    @Override String getKeyGeneratorAlgorithm() { return "AES"; }
    @Override String getCipherAlgorithm() { return "AES/CBC/PKCS5Padding"; }
    @Override int getKeyLengthInBits() { return 256; }
    @Override int getBlockSizeInBytes() { return 16; }
    @Override int getIVLengthInBytes() { return 16; }
    @Override long getMaxPlaintextSize() { return MAX_CBC_BYTES; }
}