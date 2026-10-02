package tech.harlabs.web.security;

import io.quarkus.elytron.security.common.BcryptUtil;

/**
 * Password utility using Quarkus Security BcryptUtil for high-security password hashing.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes a raw password using BCrypt with work factor 10.
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return BcryptUtil.bcryptHash(rawPassword);
    }

    /**
     * Verifies a raw password against an existing BCrypt hash.
     */
    public static boolean verify(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null) {
            return false;
        }
        return BcryptUtil.matches(rawPassword, passwordHash);
    }
}
