package com.datepoker.dp_backend.encryption;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryptionUtil {
    // ✅ Key must be exactly 32 bytes for AES-256
    private static final byte[] SECRET_KEY = "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8);
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding"; // Use CBC mode with padding

    // Generate a random IV (Initialization Vector)
    private static IvParameterSpec generateIV() {
        byte[] iv = new byte[16]; // IV must always be 16 bytes
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Encrypts data using AES-256
    public static String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY, "AES");
        Cipher cipher = Cipher.getInstance(AES_CIPHER);

        IvParameterSpec iv = generateIV();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Append IV to ciphertext before encoding
        byte[] ivAndCiphertext = new byte[iv.getIV().length + encryptedBytes.length];
        System.arraycopy(iv.getIV(), 0, ivAndCiphertext, 0, iv.getIV().length);
        System.arraycopy(encryptedBytes, 0, ivAndCiphertext, iv.getIV().length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(ivAndCiphertext);
    }

    // Decrypts data using AES-256
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY, "AES");
        Cipher cipher = Cipher.getInstance(AES_CIPHER);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);

        // Extract IV & Ciphertext
        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[decodedBytes.length - 16];
        System.arraycopy(decodedBytes, 0, iv, 0, iv.length);
        System.arraycopy(decodedBytes, iv.length, ciphertext, 0, ciphertext.length);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }
}
