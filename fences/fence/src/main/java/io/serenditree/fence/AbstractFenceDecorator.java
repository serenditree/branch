package io.serenditree.fence;

import io.serenditree.fence.annotation.FencedContext;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import io.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import io.serenditree.fence.model.api.FencePrincipal;
import jakarta.inject.Inject;

public abstract class AbstractFenceDecorator {

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
