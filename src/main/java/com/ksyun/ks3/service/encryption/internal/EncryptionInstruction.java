
package com.ksyun.ks3.service.encryption.internal;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;


/**
 * Contains information used to encrypt and decrypt objects in S3.
 */
public class EncryptionInstruction {

    private final Map<String, String> materialsDescription;
    private final byte[] encryptedSymmetricKey;
    private final Cipher symmetricCipher;
    private final CipherFactory symmetricCipherFactory;

    /**
     * Construct a new EncryptionInstruction object with the provided fields.
     *
     * @param materialsDescription
     *      The description of the encryption materials that were used to encrypt the envelope symmetric key.
     * @param encryptedSymmetricKey
     *      A byte[] array representing an encrypted envelope symmetric key.
     * @param symmetricKey
     *      The symmetric key used to create the cipher that will encrypt the object data.
     * @param symmetricCipher
     *      The symmetric cipher that will encrypt the object data.
     */
    public EncryptionInstruction(Map<String, String> materialsDescription, byte[] encryptedSymmetricKey, SecretKey symmetricKey, Cipher symmetricCipher) {
        this.materialsDescription = materialsDescription;
        this.encryptedSymmetricKey = encryptedSymmetricKey;
        this.symmetricCipher = symmetricCipher;
        this.symmetricCipherFactory = null;
    }

    public EncryptionInstruction(Map<String, String> materialsDescription, byte[] encryptedSymmetricKey, SecretKey symmetricKey, CipherFactory symmetricCipherFactory) {
        this.materialsDescription = materialsDescription;
        this.encryptedSymmetricKey = encryptedSymmetricKey;
        this.symmetricCipherFactory = symmetricCipherFactory;
        this.symmetricCipher = symmetricCipherFactory.createCipher();
    }


    public CipherFactory getCipherFactory() {
        return symmetricCipherFactory;
    }

    /**
     * Returns the description of the encryption materials that were used to encrypt the envelope symmetric key.
     *
     * @return the description of the encryption materials that were used to encrypt the envelope symmetric key.
     */
    public Map<String, String> getMaterialsDescription() {
        return this.materialsDescription;
    }

    /**
     * Returns an array of bytes representing the encrypted envelope symmetric key.
     *
     * @return an array of bytes representing the encrypted envelope symmetric key.
     */
    public byte[] getEncryptedSymmetricKey() {
        return this.encryptedSymmetricKey;
    }

    /**
     * Returns the symmetric cipher created with the envelope symmetric key.
     *
     * @return the symmetric cipher created with the envelope symmetric key.
     */
    public Cipher getSymmetricCipher() {
        return this.symmetricCipher;
    }
}
