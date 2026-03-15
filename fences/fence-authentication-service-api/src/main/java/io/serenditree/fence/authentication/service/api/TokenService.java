package io.serenditree.fence.authentication.service.api;

import io.serenditree.fence.model.api.FencePrincipal;

public interface TokenService {
    FencePrincipal authenticate(String token);

    String buildToken(FencePrincipal principal);

    String buildVerificationToken(Long id, String subject);
}
