package io.serenditree.fence.authentication.service.api;

import io.serenditree.fence.model.api.FencePrincipal;

public interface AuthenticationServiceApi {
    FencePrincipal authenticate(FencePrincipal principal);

    FencePrincipal signUp(FencePrincipal principal);

    FencePrincipal signIn(FencePrincipal principal);

    FencePrincipal verify(FencePrincipal principal, FencePrincipal oidcPrincipal);
}
