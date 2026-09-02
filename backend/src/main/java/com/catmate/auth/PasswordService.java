package com.catmate.auth;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16]; random.nextBytes(salt);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public boolean matches(String password, String encoded) {
        try {
            String[] parts = encoded.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return java.security.MessageDigest.isEqual(expected, derive(password, salt, Integer.parseInt(parts[0])));
        } catch (RuntimeException e) { return false; }
    }

    private byte[] derive(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) { throw new IllegalStateException("密码加密失败", e); }
    }
}
