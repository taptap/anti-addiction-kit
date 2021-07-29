package com.taptap.tds.registration.server.util;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.util.EncodingUtils;

public class Cryptor {

    private final KeyParameter keyParameter;

    private BytesKeyGenerator ivGenerator = KeyGenerators.secureRandom(16);

    public Cryptor(String key) {
        this(key.getBytes());
    }

    public Cryptor(byte[] key) {
        this(new KeyParameter(key));
    }

    public Cryptor(KeyParameter keyParameter) {
        this.keyParameter = keyParameter;
    }

    public String encryptToHexString(CharSequence src) {
        return new String(Hex.encode(encrypt(src)));
    }

    public byte[] encrypt(CharSequence src) {
        byte[] iv = ArrayUtils.EMPTY_BYTE_ARRAY;
        iv = ivGenerator.generateKey();
        return encrypt(src, iv);
    }

    private byte[] encrypt(CharSequence src, byte[] iv) {

        CipherParameters parameters;
        parameters = new ParametersWithIV(keyParameter, iv);
        BufferedBlockCipher cipher = new BufferedBlockCipher(new SICBlockCipher(new AESEngine()));
        cipher.init(true, parameters);

        try {
            byte[] data = src.toString().getBytes();
            byte[] encodedBytes = new byte[cipher.getOutputSize(data.length)];
            int length = cipher.processBytes(data, 0, data.length, encodedBytes, 0);
            cipher.doFinal(encodedBytes, length);

            return EncodingUtils.concatenate(iv, encodedBytes);

        } catch (InvalidCipherTextException e) {
            throw new IllegalStateException("Could not create hash", e);
        }
    }

    public byte[] decryptHexString(String src) {
        return decrypt(Hex.decode(src));
    }

    public byte[] decrypt(byte[] encrypted) {
        byte[] encryptedSecret = EncodingUtils.subArray(encrypted, this.ivGenerator.getKeyLength(), encrypted.length);
        byte[] iv = EncodingUtils.subArray(encrypted, 0, this.ivGenerator.getKeyLength());
        return decrypt(encryptedSecret, iv);
    }

    private byte[] decrypt(byte[] encryptedSecret, byte[] iv) {

        CipherParameters parameters = new ParametersWithIV(keyParameter, iv);
        BufferedBlockCipher cipher = new BufferedBlockCipher(new SICBlockCipher(new AESEngine()));;
        cipher.init(false, parameters);

        try {
            byte[] decodedBytes = new byte[cipher.getOutputSize(encryptedSecret.length)];
            int length = cipher.processBytes(encryptedSecret, 0, encryptedSecret.length, decodedBytes, 0);
            cipher.doFinal(decodedBytes, length);

            return decodedBytes;

        } catch (InvalidCipherTextException e) {
            throw new IllegalStateException("Could not create hash", e);
        }
    }
}
