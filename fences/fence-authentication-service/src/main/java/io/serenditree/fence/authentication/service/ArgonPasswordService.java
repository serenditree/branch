package io.serenditree.fence.authentication.service;

import io.serenditree.fence.authentication.service.api.PasswordService;
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
public class ArgonPasswordService implements PasswordService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private static final String VARIANT_IDENTIFIER = "argon2id";
    private static final int VARIANT = Argon2Parameters.ARGON2_id;
    private static final int VERSION = Argon2Parameters.ARGON2_VERSION_13;
    private static final int MEMORY = (int) Math.pow(2, 16) - 1;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;

    private static final String PHC_PARAMETERS = String.format(
        "$%s$v=%d$m=%d,t=%d,p=%d",
        VARIANT_IDENTIFIER,
        VERSION,
        MEMORY,
        ITERATIONS,
        PARALLELISM
    );

    @SuppressWarnings("java:S6218" )
    private record Argon2Record(Argon2Parameters parameters, byte[] hash) {
    }

    private enum PARAMETER {
        PRECURSOR, VARIANT, VERSION, COSTS, SALT, HASH
    }

    private enum COST {
        MEMORY, ITERATIONS, PARALLELISM
    }

    /**
     * Checks if a password meets a password policy and hashes it using Argon2.
     *
     * @param plainText Password as plain text.
     * @return A string in PHC format.
     */
    @Override
    public String hash(final String plainText) {
        if (!OakPassword.password(plainText)) {
            throw new BadRequestException("Password too weak");
        }

        final byte[] salt = this.salt();
        final byte[] hash = this.hash(
            plainText,
            new Argon2Parameters
                .Builder(VARIANT)
                .withVersion(VERSION)
                .withMemoryAsKB(MEMORY)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build()
        );

        return this.toPHCStringFormat(salt, hash);
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
        if (!OakPassword.passwordHash(hash)) {
            throw new IllegalArgumentException("Invalid password hash: " + hash);
        }

        final Argon2Record argon2Record = this.fromPHCStringFormat(hash);

        return MessageDigest.isEqual(
            argon2Record.hash,
            this.hash(plainText, argon2Record.parameters)
        );
    }

    /**
     * Hashes a plain text password using Argon2.
     *
     * @param plainText        Password as plain text.
     * @param argon2Parameters Argon2 parameters.
     * @return Hashed password as a byte array.
     */
    private byte[] hash(final String plainText, final Argon2Parameters argon2Parameters) {
        final Argon2BytesGenerator argon2 = new Argon2BytesGenerator();
        argon2.init(argon2Parameters);

        final byte[] hash = new byte[HASH_LENGTH];
        argon2.generateBytes(
            plainText.getBytes(StandardCharsets.UTF_8),
            hash,
            0,
            hash.length
        );

        return hash;
    }

    /**
     * Generates a random salt.
     *
     * @return Random salt as byte array.
     */
    private byte[] salt() {
        final byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        return salt;
    }

    /**
     * Converts the provided salt and hash into a password hashing competition (PHC) compliant string.
     *
     * @param salt The random salt used during the hashing process.
     * @param hash The hash of the password.
     * @return A string in PHC string format.
     * @see <a href="https://github.com/P-H-C/phc-string-format/blob/master/phc-sf-spec.md">PHC String Format</a>
     */
    private String toPHCStringFormat(final byte[] salt, final byte[] hash) {

        return PHC_PARAMETERS
               + "$" + Base64.toBase64String(salt)
               + "$" + Base64.toBase64String(hash);
    }

    /**
     * Parses a hash in password hashing competition (PHC) string format into its constituent parts.
     *
     * @param hash The hash in PHC string format.
     * @return An Argon2Record containing the parsed components.
     * @see <a href="https://github.com/P-H-C/phc-string-format/blob/master/phc-sf-spec.md">PHC String Format</a>
     */
    private Argon2Record fromPHCStringFormat(final String hash) {
        final String[] parameters = hash.split("\\$");
        final String[] costs = parameters[PARAMETER.COSTS.ordinal()].split(",");

        return new Argon2Record(
            new Argon2Parameters
                .Builder(VARIANT)
                .withVersion(Integer.parseInt(parameters[PARAMETER.VERSION.ordinal()].split("=")[1]))
                .withMemoryAsKB(this.getCost(COST.MEMORY, costs))
                .withIterations(this.getCost(COST.ITERATIONS, costs))
                .withParallelism(this.getCost(COST.PARALLELISM, costs))
                .withSalt(Base64.decode(parameters[PARAMETER.SALT.ordinal()]))
                .build(),
            Base64.decode(parameters[PARAMETER.HASH.ordinal()])
        );
    }

    /**
     * Retrieves the specified cost parameter value from the provided cost parameters.
     *
     * @param cost  Cost parameter to retrieve.
     * @param costs Array of cost parameters.
     * @return Cost parameter value.
     */
    private int getCost(final COST cost, final String[] costs) {
        final String costParameterValue = costs[cost.ordinal()].split("=")[1];

        return Integer.parseInt(costParameterValue);
    }
}
