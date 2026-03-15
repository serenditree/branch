package io.serenditree.fence;

import io.serenditree.fence.annotation.FencedContext;
import io.serenditree.fence.model.FenceContext;
import io.serenditree.fence.model.api.FencePrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

/**
 * Observes authentication events and provides custom security context objects for CDI managed services.
 */
@RequestScoped
public class FenceContextProducer {

    private FenceContext fenceContext;

    private FencePrincipal principal;

    public void handleAuthenticationEvent(@Observes @FencedContext FenceContext fenceContext) {
        this.fenceContext = fenceContext;
        this.principal = fenceContext.getUserPrincipal();
    }

    /**
     * Producer method for fenceContext (including Principal).
     *
     * @return value of fenceContext.
     */
    @Produces
    @RequestScoped
    @FencedContext
    public FenceContext getFenceContext() {
        return fenceContext;
    }

    /**
     * Producer method for principal.
     *
     * @return value of principal.
     */
    @Produces
    @RequestScoped
    @FencedContext
    public FencePrincipal getPrincipal() {
        return principal;
    }
}
