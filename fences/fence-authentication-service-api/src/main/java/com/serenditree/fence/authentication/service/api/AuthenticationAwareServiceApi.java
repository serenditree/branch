package com.serenditree.fence.authentication.service.api;

import com.serenditree.fence.model.api.FencePrincipal;

public interface AuthenticationAwareServiceApi {
    FencePrincipal signUp(FencePrincipal principal);

    FencePrincipal retrievePrincipalByUsername(String username);

    void verify(FencePrincipal principal);
}
