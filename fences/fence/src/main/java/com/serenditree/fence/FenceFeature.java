package com.serenditree.fence;

import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.authentication.service.api.AuthenticationServiceApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.FenceContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Registers security filters for all requests and enables CDI in request filter.
 * Starting point of the "Fence" security feature.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FenceFeature implements DynamicFeature {

    @Inject
    @ConfigProperty(name = "serenditree.force.https", defaultValue = "false")
    Instance<Boolean> forceHttps;

    private AuthenticationServiceApi authenticationService;

    private AuthorizationServiceApi authorizationService;

    private Event<FenceContext> authenticationEvent;

    /**
     * Registers a {@link FenceFilter} for all requests.
     *
     * @param resourceInfo   {@link ResourceInfo}
     * @param featureContext {@link FeatureContext}
     */
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        featureContext.register(new FenceFilter(
                resourceInfo,
                this.authenticationService,
                this.authorizationService,
                this.authenticationEvent,
                this.forceHttps.get()
        ));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setAuthenticationService(AuthenticationServiceApi authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Inject
    public void setAuthorizationService(AuthorizationServiceApi authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Inject
    public void setAuthenticationEvent(@FencedContext Event<FenceContext> authenticationEvent) {
        this.authenticationEvent = authenticationEvent;
    }
}
