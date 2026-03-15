package io.serenditree.fence.authentication.service.api;

import io.serenditree.fence.model.api.FencePrincipal;

public interface AuthenticationAwareServiceApi {
    FencePrincipal signUp(FencePrincipal principal);

    FencePrincipal retrievePrincipalByUsername(String username);

    void verify(FencePrincipal principal);
}
