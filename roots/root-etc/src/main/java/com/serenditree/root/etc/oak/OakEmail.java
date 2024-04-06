package com.serenditree.root.etc.oak;

import java.util.regex.Pattern;

/**
 * Class for deciding globally if email input is oak or nut(s).
 */
public class OakEmail {

    private OakEmail() {
    }

    /**
     * Pattern that matches valid email addresses.
     */
    public static final String EMAIL_PATTERN_STRING = "[a-zA-Z\\d._\\-]+[a-zA-Z\\d]@[a-z]+(\\.[a-z]+)+";

    /**
     * Compiled pattern for valid email addresses.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(OakEmail.EMAIL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

    /**
     * Checks if a {@link String} matches the pattern of a valid email address.
     *
     * @param email Email address as {@link String}.
     * @return boolean
     */
    public static boolean email(String email) {

        return OakEmail.EMAIL_PATTERN.matcher(email).matches();
    }
}
