package io.serenditree.fence.authentication.service.api;

public interface PasswordService {
    String hash(final String plainText);

    boolean verify(final String hash, final String plainText);
}
