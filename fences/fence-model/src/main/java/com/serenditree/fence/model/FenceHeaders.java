package com.serenditree.fence.model;

public abstract class FenceHeaders {

    private FenceHeaders() {
    }

    public static final String PREFIX = "X-ST-";
    public static final String ID = PREFIX + "ID";
    public static final String USERNAME = PREFIX + "Username";
    public static final String PASSWORD = PREFIX + "Password";
    public static final String EMAIL = PREFIX + "Email";
    public static final String VERIFICATION = PREFIX + "Verification";
}
