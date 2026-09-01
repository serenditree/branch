package io.serenditree.fence;

import io.serenditree.fence.annotation.FenceContext;
import io.serenditree.fence.model.api.FencePrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

/**
 * Observes authentication events and provides custom security context objects for CDI managed services.
 */
@RequestScoped
public class FenceContextProducer {

    private io.serenditree.fence.model.FenceContext fenceContext;
    private FencePrincipal principal;

    public void handleAuthenticationEvent(
        @Observes @FenceContext io.serenditree.fence.model.FenceContext fenceContext
    ) {
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
    @FenceContext
    public io.serenditree.fence.model.FenceContext getFenceContext() {
        return fenceContext;
    }

    /**
     * Producer method for principal.
     *
     * @return value of principal.
     */
    @Produces
    @RequestScoped
    @FenceContext
    public FencePrincipal getPrincipal() {
        return principal;
    }
}
