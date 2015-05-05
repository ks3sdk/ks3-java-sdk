package com.ksyun.ks3.service.encryption.internal;

import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Factory for creating Ciphers based on the crypto key and configuration
 * specified in the constructor.
 *
 * Each invocation of {@link #createCipher()} will produce a new, identical
 * Cipher object. Each returned Cipher will encrypt data with
 * <bold>exactly</bold> the same output as any other Cipher returned by this
 * instance of CipherFactory.
 */
public class CipherFactory {
    private final SecretKey symmetricKey;
    private final int cipherMode;
    private byte[] initVectorBytes;
    private final Provider cryptoProvider;

    /**
     * Creates a new CipherFactory that will produce ciphers using the specified
     * key, mode, IV and provider.
     *
     * @param symmetricKey
     *            The symmetric key used in the ciphers created by this factory.
     * @param cipherMode
     *            The mode indicating whether the created ciphers are for
     *            encrypting or decrypting.
     * @param initVectorBytes
     *            The optional crypto IV used to initialize the ciphers.
     * @param cryptoProvider
     *            The optional Java crypto provider implementation, if an
     *            alternate crypto provider is to be used.
     */
    public CipherFactory(SecretKey symmetricKey, int cipherMode, byte[] initVectorBytes, Provider cryptoProvider) {
        this.symmetricKey = symmetricKey;
        this.cipherMode = cipherMode;
        this.initVectorBytes = initVectorBytes;
        this.cryptoProvider = cryptoProvider;
    }

    /**
     * Creates a new Cipher instance based on the crypto parameters specified in
     * the constructor. Each Cipher instance is identical to any other Cipher
     * produced by this CipherFactory.
     *
     * @return A new Cipher instance, identical to each Cipher produced by this
     *         factory instance.
     */
    public Cipher createCipher() {
        Cipher cipher = EncryptionUtils.createSymmetricCipher(symmetricKey, cipherMode, cryptoProvider, initVectorBytes);

        // If we weren't given an IV to use, make sure we store the one the first Cipher
        // used so we can use it for all future Ciphers created by this instance.
        if (initVectorBytes == null) initVectorBytes = cipher.getIV();

        return cipher;
    }

    public Provider getCryptoProvider() {
        return cryptoProvider;
    }

    public int getCipherMode() {
        return cipherMode;
    }

    public byte[] getIV() {
        return initVectorBytes == null ? null : initVectorBytes.clone();
    }
}