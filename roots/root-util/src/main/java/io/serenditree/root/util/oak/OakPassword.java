package io.serenditree.root.util.oak;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for deciding globally if password input is oak or nut(s).
 */
public class OakPassword {

    private OakPassword() {
    }

    private static final int MIN_ENTROPY = 64;

    private static final int EFF_WORD_LIST_COUNT = 7776;

    /**
     * Pattern that matches a word of a word-list with arbitrary delimiter.
     */
    public static final String WORD_LIST_PATTERN_STRING = "(([a-zA-z]{2,})(?=[^a-zA-z]+|$)){5,}";

    /**
     * Compiled word-list pattern.
     */
    public static final Pattern WORD_LIST_PATTERN = Pattern.compile(OakPassword.WORD_LIST_PATTERN_STRING);

    /**
     * Pattern that matches an Argon2 hash.
     */
    public static final String HASHED_PASSWORD_PATTERN_STRING = "^\\$argon2[di]d?\\$v=\\d+\\$m=\\d+,t=\\d,p=\\d\\$.+$";

    /**
     * Pattern that matches a password which consists of numbers, uppercase, lowercase and special characters or a
     * word list or an Argon2 hash.
     */
    public static final String PASSWORD_PATTERN_STRING =
        "(((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\da-zA-Z]).+" +
        ")|(" +
        WORD_LIST_PATTERN_STRING +
        ")|(" +
        HASHED_PASSWORD_PATTERN_STRING +
        "))";

    /**
     * Checks if the passwords' entropy is high enough.
     *
     * @param plainText Unencrypted password
     * @return boolean
     */
    public static boolean password(String plainText) {
        int count = 0;
        int pool = EFF_WORD_LIST_COUNT;

        Matcher wordListMatcher = WORD_LIST_PATTERN.matcher(plainText);

        if (!wordListMatcher.matches()) {
            count = plainText.length();
            pool = 0;

            if (plainText.matches(".*[a-z].*")) {
                pool += 26;
            }
            if (plainText.matches(".*[A-Z].*")) {
                pool += 26;
            }
            if (plainText.matches(".*\\d.*")) {
                pool += 10;
            }
            if (plainText.matches(".*[^a-zA-Z\\d].*")) {
                pool += 33;
            }
        }

        double entropy = Math.log(Math.pow(pool, count)) / Math.log(2);

        return entropy >= MIN_ENTROPY;
    }
}
