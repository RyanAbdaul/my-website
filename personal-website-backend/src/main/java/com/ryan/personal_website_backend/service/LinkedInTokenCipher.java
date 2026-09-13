package com.ryan.personal_website_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class LinkedInTokenCipher {
    private static final int IV_BYTES = 12;
    private final String encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public LinkedInTokenCipher(@Value("${app.linkedin.token-encryption-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String encrypt(String value) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encrypt the LinkedIn access token", exception);
        }
    }

    public boolean isConfigured() {
        try {
            return encryptionKey != null && !encryptionKey.isBlank() && Base64.getDecoder().decode(encryptionKey).length == 32;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String decrypt(String value) {
        try {
            byte[] bytes = Base64.getDecoder().decode(value);
            byte[] iv = java.util.Arrays.copyOfRange(bytes, 0, IV_BYTES);
            byte[] encrypted = java.util.Arrays.copyOfRange(bytes, IV_BYTES, bytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not decrypt the LinkedIn access token", exception);
        }
    }

    private SecretKeySpec key() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("LINKEDIN_TOKEN_ENCRYPTION_KEY must be a base64-encoded 32-byte AES key");
        }
        byte[] decoded = Base64.getDecoder().decode(encryptionKey);
        if (decoded.length != 32) {
            throw new IllegalStateException("LINKEDIN_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
        }
        return new SecretKeySpec(decoded, "AES");
    }
}
