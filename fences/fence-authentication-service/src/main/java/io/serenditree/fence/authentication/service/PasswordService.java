package io.serenditree.fence.authentication.service;

import io.serenditree.fence.authentication.service.api.PasswordServiceApi;
import io.serenditree.root.util.oak.OakPassword;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.BadRequestException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Hashes and verifies passwords using Argon2.
 */
@Dependent
public class PasswordService implements PasswordServiceApi {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 4;

    /**
     * Checks if a password meets a password policy and hashes it using Argon2.
     *
     * @param plainText Password as plain text.
     * @return Hashed password or null.
     */
    @Override
    public String hash(final String plainText) {

        if (!OakPassword.password(plainText)) {
            throw new BadRequestException("Password too weak");
        }

        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        // Format: $argon2id$v=19$m=65536,t=3,p=4$salt$hash
        return String.format(
            "$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
            MEMORY, ITERATIONS, PARALLELISM,
            Base64.toBase64String(salt),
            Base64.toBase64String(this.hash(plainText, MEMORY, ITERATIONS, PARALLELISM, salt))
        );
    }

    /**
     * Checks if a plain text password matches its Argon2 hash.
     *
     * @param hash      Argon 2 hash of password.
     * @param plainText Plain-text password.
     * @return boolean
     */
    @Override
    public boolean verify(final String hash, final String plainText) {
        boolean verified = false;

        String[] parts = hash.split("\\$");
        if (parts.length == 6) {
            // Parse parameters from parts[3] -> m=65536,t=3,p=4
            String[] params = parts[3].split(",");
            int memory = Integer.parseInt(params[0].split("=")[1]);
            int iterations = Integer.parseInt(params[1].split("=")[1]);
            int parallelism = Integer.parseInt(params[2].split("=")[1]);

            byte[] salt = Base64.decode(parts[4]);
            byte[] originalHash = Base64.decode(parts[5]);

            verified = MessageDigest.isEqual(
                originalHash,
                this.hash(plainText, memory, iterations, parallelism, salt)
            );

        }

        return verified;
    }

    /**
     * Hashes a plain text password using Argon2.
     *
     * @param plainText   Password as plain text.
     * @param memory      Argon2 memory parameter.
     * @param iterations  Argon2 iterations parameter.
     * @param parallelism Argon2 parallelism parameter.
     * @param salt        Salt.
     * @return hashed password
     */
    private byte[] hash(String plainText, int memory, int iterations, int parallelism, byte[] salt) {
        Argon2BytesGenerator argon2 = new Argon2BytesGenerator();
        argon2.init(
            new Argon2Parameters
                .Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iterations)
                .withMemoryAsKB(memory)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build()
        );

        byte[] hash = new byte[HASH_LENGTH];
        argon2.generateBytes(plainText.getBytes(StandardCharsets.UTF_8), hash, 0, hash.length);

        return hash;
    }
}
