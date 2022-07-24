package com.serenditree.fence;

import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.api.FencePrincipal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

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
