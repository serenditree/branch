package io.serenditree.fence;

import io.serenditree.fence.annotation.FenceContext;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.fence.authorization.service.api.AuthorizationService;
import io.serenditree.fence.model.api.FencePrincipal;
import jakarta.inject.Inject;

public abstract class AbstractFenceDecorator {

    protected FencePrincipal principal;
    protected AuthorizationService authorizationService;
    protected AuthorizationRepository authorizationRepository;

    @Inject
    protected void setPrincipal(@FenceContext FencePrincipal principal) {
        this.principal = principal;
    }

    @Inject
    protected void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Inject
    protected void setAuthorizationRepository(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
