package io.serenditree.root.util.oak;

import java.util.regex.Pattern;

/**
 * Class for deciding globally if password input is oak or nut(s).
 */
public class OakPassword {
    /**
     * Regular expression that matches a word of a word-list with arbitrary delimiter.
     */
    public static final String WORD_LIST_REGEX = "(([a-zA-z]{2,})(?=[^a-zA-z]+|$)){5,}";
    /**
     * Compiled word-list pattern.
     */
    public static final Pattern WORD_LIST_PATTERN = Pattern.compile(OakPassword.WORD_LIST_REGEX);
    /**
     * Regular expression that matches an Argon2 hash.
     */
    public static final String HASHED_PASSWORD_REGEX = "^\\$argon2([di]|id)\\$v=\\d+\\$m=\\d+,t=\\d+,p=\\d+\\$.+\\$.+$";
    /**
     * Compiled Argon2 hash pattern.
     */
    public static final Pattern HASHED_PASSWORD_PATTERN = Pattern.compile(OakPassword.HASHED_PASSWORD_REGEX);
    /**
     * Regular expression that matches a password which consists of numbers, uppercase, lowercase, and special
     * characters or a word list or an Argon2 hash.
     */
    public static final String PASSWORD_REGEX = "(((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\da-zA-Z]).+)|("
        + WORD_LIST_REGEX + ")|("
        + HASHED_PASSWORD_REGEX + "))";

    private static final int MIN_ENTROPY = 64;
    private static final int EFF_WORD_LIST_COUNT = 7776;

    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGITS = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^a-zA-Z\\d].*");

    private OakPassword() {
    }

    /**
     * Checks if the passwords' entropy is high enough.
     *
     * @param plainText Unencrypted password
     * @return boolean
     */
    public static boolean password(final String plainText) {
        int count = 0;
        int pool = EFF_WORD_LIST_COUNT;

        if (!WORD_LIST_PATTERN.matcher(plainText).matches()) {
            count = plainText.length();
            pool = 0;

            if (LOWERCASE.matcher(plainText).matches()) {
                pool += 26;
            }
            if (UPPERCASE.matcher(plainText).matches()) {
                pool += 26;
            }
            if (DIGITS.matcher(plainText).matches()) {
                pool += 10;
            }
            if (SPECIAL.matcher(plainText).matches()) {
                pool += 33;
            }
        }

        double entropy = Math.log(Math.pow(pool, count)) / Math.log(2);

        return entropy >= MIN_ENTROPY;
    }

    /**
     * Checks if the hash is an Argon2 hash.
     *
     * @param hash Argon 2 hash
     * @return boolean
     */
    public static boolean passwordHash(final String hash) {

        return HASHED_PASSWORD_PATTERN
            .matcher(hash)
            .matches();
    }
}
