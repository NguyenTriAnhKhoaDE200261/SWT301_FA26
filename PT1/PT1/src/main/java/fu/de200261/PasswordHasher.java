package fu.de200261;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordHasher {

    private PasswordHasher() {
        // Constructor private
    }

    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        return HexFormat.of().formatHex(saltBytes);
    }

    public static String hash(String salt, String rawPassword) {
        if (salt == null || rawPassword == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matches(String salt, String rawPassword, String hashedPassword) {
        if (salt == null || rawPassword == null || hashedPassword == null) return false;
        String computedHash = hash(salt, rawPassword);
        return computedHash.equals(hashedPassword);
    }
}
