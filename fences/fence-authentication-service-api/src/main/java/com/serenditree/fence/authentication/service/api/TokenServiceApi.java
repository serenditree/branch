package com.serenditree.fence.authentication.service.api;

import com.serenditree.fence.model.api.FencePrincipal;

public interface TokenServiceApi {
    FencePrincipal authenticate(String token);

    String buildToken(FencePrincipal principal);

    String buildVerificationToken(Long id, String subject);
}
