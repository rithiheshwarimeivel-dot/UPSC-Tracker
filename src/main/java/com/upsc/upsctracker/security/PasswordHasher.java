package com.upsc.upsctracker.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final int ITERATIONS = 600_000;

    private static final int SALT_LENGTH = 16;

    private static final int KEY_LENGTH = 256;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {

        try {

            byte[] salt =
                    new byte[SALT_LENGTH];

            SECURE_RANDOM.nextBytes(salt);

            byte[] hash =
                    generateHash(
                            password,
                            salt,
                            ITERATIONS
                    );

            return "pbkdf2_sha256$"
                    + ITERATIONS
                    + "$"
                    + Base64.getEncoder()
                    .encodeToString(salt)
                    + "$"
                    + Base64.getEncoder()
                    .encodeToString(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to hash password.",
                    e
            );
        }
    }

    public static boolean matches(
            String password,
            String storedPassword) {

        try {

            if (password == null ||
                    storedPassword == null) {

                return false;
            }

            if (!storedPassword.startsWith(
                    "pbkdf2_sha256$")) {

                return false;
            }

            String[] parts =
                    storedPassword.split("\\$");

            if (parts.length != 4) {

                return false;
            }

            int iterations =
                    Integer.parseInt(parts[1]);

            byte[] salt =
                    Base64.getDecoder()
                            .decode(parts[2]);

            byte[] expectedHash =
                    Base64.getDecoder()
                            .decode(parts[3]);

            byte[] actualHash =
                    generateHash(
                            password,
                            salt,
                            iterations
                    );

            return MessageDigest.isEqual(
                    expectedHash,
                    actualHash
            );

        } catch (Exception e) {

            return false;
        }
    }

    private static byte[] generateHash(
            String password,
            byte[] salt,
            int iterations)
            throws Exception {

        PBEKeySpec spec =
                new PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        iterations,
                        KEY_LENGTH
                );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            ALGORITHM
                    );

            return factory
                    .generateSecret(spec)
                    .getEncoded();

        } finally {

            spec.clearPassword();
        }
    }
}