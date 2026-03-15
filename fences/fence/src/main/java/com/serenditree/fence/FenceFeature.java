package com.serenditree.fence;

import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.authentication.service.api.AuthenticationServiceApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.FenceContext;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Registers security filters for all requests and enables CDI in request filter.
 * Starting point of the "Fence" security feature.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FenceFeature implements DynamicFeature {

    Instance<Boolean> forceHttps;

    private final AuthenticationServiceApi authenticationService;

    private final AuthorizationServiceApi authorizationService;

    private final Event<FenceContext> authenticationEvent;

    private final Tracer fenceTracer;

    /**
     * Constructor for the FenceFeature class. Initializes dependencies required for the security feature configuration.
     *
     * @param forceHttps            Instance managing configuration property for enforcing HTTPS communication.
     * @param authenticationService Service responsible for managing authentication processes.
     * @param authorizationService  Service responsible for handling authorization logic.
     * @param authenticationEvent   CDI event used for propagating security context during authentication.
     * @param fenceTracer           Tracer tool for monitoring and tracing security-related requests.
     */
    @Inject
    public FenceFeature(
        @ConfigProperty(name = "serenditree.force.https", defaultValue = "false") Instance<Boolean> forceHttps,
        AuthenticationServiceApi authenticationService,
        AuthorizationServiceApi authorizationService,
        @FencedContext Event<FenceContext> authenticationEvent,
        Tracer fenceTracer
    ) {
        this.forceHttps = forceHttps;
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.authenticationEvent = authenticationEvent;
        this.fenceTracer = fenceTracer;
    }

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
            this.fenceTracer,
            this.forceHttps.get()
        ));
    }
}
