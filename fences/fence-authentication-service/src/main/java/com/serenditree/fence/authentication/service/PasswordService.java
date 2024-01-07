package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.PasswordServiceApi;
import com.serenditree.root.etc.oak.Oak;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.BadRequestException;

/**
 * Hashes and verifies passwords using Argon2.
 */
@Dependent
public class PasswordService implements PasswordServiceApi {

    private static final Argon2 ARGON_2 = Argon2Factory.create();

    private static final int MAX_EXECUTION_TIME_MILLISECONDS = 1000;

    private static final int COST_N_MEMORY = (int) Math.pow(2, 16) - 1;

    private static final int COST_P_PARALLELISM = 4;

    private static final int COST_R_ITERATIONS;

    static {
        if ("test".equals(System.getProperty("serenditree.context"))) {
            COST_R_ITERATIONS = 2;
        } else {
            COST_R_ITERATIONS = Argon2Helper.findIterations(
                ARGON_2,
                MAX_EXECUTION_TIME_MILLISECONDS,
                COST_N_MEMORY,
                COST_P_PARALLELISM
            );
        }
    }

    /**
     * Checks if a password meets a password policy and hashes it using Argon 2.
     *
     * @param plainText Password as plain text.
     *                  the password policy.
     * @return Hashed password or null.
     * @see de.mkammerer.argon2.Argon2
     */
    @Override
    public String hash(final String plainText) {

        String hash;

        if (Oak.password(plainText)) {
            char[] plainTextCharArray = plainText.toCharArray();
            try {
                hash = ARGON_2.hash(
                    PasswordService.COST_R_ITERATIONS,
                    PasswordService.COST_N_MEMORY,
                    PasswordService.COST_P_PARALLELISM,
                    plainTextCharArray
                );
            } finally {
                ARGON_2.wipeArray(plainTextCharArray);
            }
        } else {
            throw new BadRequestException("Password too weak");
        }

        return hash;
    }

    /**
     * Checks if a plain text password matches its Argon 2 hash.
     *
     * @param hash      Argon 2 hash of password.
     * @param plainText Plain-text password.
     * @return boolean
     * @see de.mkammerer.argon2.Argon2
     */
    @Override
    public boolean verify(final String hash, final String plainText) {

        return ARGON_2.verify(hash, plainText.toCharArray());
    }
}
