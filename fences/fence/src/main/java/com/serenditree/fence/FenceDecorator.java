package com.serenditree.fence;

import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.api.FencePrincipal;
import jakarta.inject.Inject;

public abstract class FenceDecorator {

    protected FencePrincipal principal;

    protected AuthorizationServiceApi authorizationService;

    protected AuthorizationRepositoryApi authorizationRepository;

    @Inject
    public void setPrincipal(@FencedContext FencePrincipal principal) {
        this.principal = principal;
    }

    @Inject
    public void setAuthorizationService(AuthorizationServiceApi authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
