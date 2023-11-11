package testers.util;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public final class SdkCrypto {
    private static final String DEFAULT_ALGORITHM = "AES";
    private static final String CIPHER_PADDING = DEFAULT_ALGORITHM + "/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int VECTOR_SIZE = 16;

    // utils class
    private SdkCrypto() {}

    private static final KeyGenerator symKeyGenerator = newKeyGenerator();

    /**
     * Create a new AES-256 key that will never be stored. You can use this key to encrypt ephemeral data, usually a
     * temporary file on local disk. Never store this key in persistent storage like disk, s3 or a database!
     */
    public static SecretKey newEphemeralKey() {
        return symKeyGenerator.generateKey();
    }

    private static KeyGenerator newKeyGenerator() {
        try {
            KeyGenerator symKeyGenerator = KeyGenerator.getInstance(DEFAULT_ALGORITHM);
            symKeyGenerator.init(KEY_SIZE);
            return symKeyGenerator;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Encrypt an outgoing stream of data */
    public static CipherOutputStream encryptWrite(OutputStream out, SecretKey key) throws IOException {
        Cipher cipher = cipher(key, Optional.empty(), false);
        out.write(cipher.getIV());
        return new CipherOutputStream(out, cipher);
    }

    /** Decrypt an incoming stream of data */
    public static CipherInputStream decryptRead(InputStream in, SecretKey key) throws IOException {
        byte[] iv = new byte[VECTOR_SIZE];
        int readCount = in.read(iv);
        if (readCount != VECTOR_SIZE) throw new RuntimeException("Read wrong number of bytes " + readCount);

        return new CipherInputStream(in, cipher(key, Optional.of(iv), true));
    }

    public static Cipher cipher(SecretKey key, Optional<byte[]> ivInit, boolean decrypt) {
        try {
            byte[] iv = ivInit.orElseGet(SdkCrypto::newInitializationVector);
            assert iv.length == VECTOR_SIZE
                    : "Initialization vector should be " + VECTOR_SIZE + " bytes but was " + iv.length;

            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            cipher.init(decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher;
        } catch (InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException e) {
            throw new RuntimeException("Error constructing cipher from secret key!", e);
        }
    }

    public static byte[] newInitializationVector() {
        byte[] result = new byte[VECTOR_SIZE];
        SecureRandom random = new SecureRandom();

        random.nextBytes(result);

        return result;
    }
}
