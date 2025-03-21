package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    @BeforeEach
    void setUp() {
        // No dependencies, as AESEncryptionUtil is a static utility class
    }

    @Test
    void testEncryptionAndDecryption_Success() {
        // ✅ Arrange
        String originalText = "Hello, Encryption!";

        // 🔐 Encrypt
        String encryptedText = AESEncryptionUtil.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText, "Encrypted text should not match original text");

        // 🔓 Decrypt
        String decryptedText = AESEncryptionUtil.decrypt(encryptedText);
        assertNotNull(decryptedText);
        assertEquals(originalText, decryptedText, "Decrypted text should match original");
    }

    @Test
    void testEncryption_ReturnsDifferentResults() {
        // ✅ Arrange
        String text = "Hello, Encryption!";

        // 🔐 Encrypt Twice
        String encryptedText1 = AESEncryptionUtil.encrypt(text);
        String encryptedText2 = AESEncryptionUtil.encrypt(text);

        // 🚀 Each encryption should produce different ciphertexts due to unique IV
        assertNotEquals(encryptedText1, encryptedText2, "Each encryption should produce a unique result");
    }

    @Test
    void testDecryptionFailsWithCorruptedData() {
        // 🔐 Encrypt a valid string
        String encryptedText = AESEncryptionUtil.encrypt("Test message");

        // 🚀 Corrupt the encrypted text by modifying characters
        String corruptedText = encryptedText.substring(0, encryptedText.length() - 2) + "xx";

        // 🛠 Act & Assert
        Exception exception = assertThrows(EncryptionException.class, () -> {
            AESEncryptionUtil.decrypt(corruptedText);
        });

        assertTrue(exception.getMessage().contains("Decryption failed"), "Corrupted data should fail decryption");
    }

    @Test
    void testDecryptionFailsWithInvalidBase64() {
        // 🛠 Act & Assert
        Exception exception = assertThrows(EncryptionException.class, () -> {
            AESEncryptionUtil.decrypt("%%%INVALID-BASE64-DATA%%%");
        });

        assertTrue(exception.getMessage().contains("Invalid Base64 input"), "Exception message should indicate Base64 decoding failure");
    }
}
