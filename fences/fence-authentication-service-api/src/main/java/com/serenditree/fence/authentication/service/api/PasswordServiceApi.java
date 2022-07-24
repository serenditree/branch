package com.serenditree.fence.authentication.service.api;

public interface PasswordServiceApi {
    String hash(final String plainText);

    boolean verify(final String hash, final String plainText);
}
