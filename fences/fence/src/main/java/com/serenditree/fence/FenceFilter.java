package com.serenditree.fence;

import com.serenditree.fence.annotation.*;
import com.serenditree.fence.authentication.service.api.AuthenticationServiceApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.root.rest.transfer.ApiResponse;
import com.serenditree.root.util.maple.Maple;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Custom {@link ContainerRequestFilter} that handles authentication and authorization before a request reaches the
 * requested resource method. Security information is added to the {@link FenceContext}.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FenceFilter implements ContainerRequestFilter {

    private static final String FENCE_TRACER_FILTER = FenceFilter.class.getSimpleName() + ".filter";

    private static final String UNHANDLED_ERROR = ConfigProvider
        .getConfig()
        .getValue("serenditree.fence.error", String.class);

    private final jakarta.inject.Provider<AuthenticationServiceApi> authenticationService;

    private final jakarta.inject.Provider<AuthorizationServiceApi> authorizationService;

    private final jakarta.inject.Provider<Event<FenceContext>> authenticationEvent;

    private final jakarta.inject.Provider<Tracer> fenceTracer;

    private final ResourceInfo resourceInfo;

    @Inject
    public FenceFilter(jakarta.inject.Provider<AuthenticationServiceApi> authenticationService,
                       jakarta.inject.Provider<AuthorizationServiceApi> authorizationService,
                       @FencedContext jakarta.inject.Provider<Event<FenceContext>> authenticationEvent,
                       jakarta.inject.Provider<Tracer> fenceTracer,
                       @Context ResourceInfo resourceInfo) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.authenticationEvent = authenticationEvent;
        this.fenceTracer = fenceTracer;
        this.resourceInfo = resourceInfo;
    }

    /**
     * Collects request information and dispatches to auth-methods when preconditions are met. If a resource method
     * does not contain any authorization information, ie an annotation of type {@link Fenced} or {@link Open} the
     * request is aborted.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     */
    @Override
    public void filter(final ContainerRequestContext containerRequestContext) {
        final Span fenceSpan = this.fenceTracer.get().spanBuilder(FENCE_TRACER_FILTER)
            .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try {
            final String request = "Request [" + containerRequestContext.getUriInfo().getPath() + "]";
            // Assure that authorization is defined and that the client is authorized to request the
            // target resource!
            if (this.resourceInfo.getResourceMethod().isAnnotationPresent(Fenced.class)) {
                Log.debugv("{0} needs authorization or is for authentication purposes.", request);
                this.applyAuthenticationAndAuthorization(containerRequestContext);
            } else if (!this.resourceInfo.getResourceMethod().isAnnotationPresent(Open.class)) {
                // To avoid unintentional access, (new) resources without auth-annotations are blocked per default.
                Log.errorv("{0} has no authorization information.", request);
                this.abortWith(
                    containerRequestContext,
                    Response.Status.FORBIDDEN,
                    request + " is not yet available."
                );
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            Log.error(UNHANDLED_ERROR, e);
            this.abortWith(
                containerRequestContext,
                Response.Status.INTERNAL_SERVER_ERROR,
                UNHANDLED_ERROR
            );
        } finally {
            fenceSpan.end();
        }
    }

    /**
     * Dispatches to sign up/in methods or methods for authentication and authorization.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     */
    private void applyAuthenticationAndAuthorization(final ContainerRequestContext containerRequestContext) {

        FencePrincipal notAuthenticatedUser = this.createPrincipal(containerRequestContext);

        if (StringUtils.isNotBlank(notAuthenticatedUser.getToken())) {
            // AUTHENTICATION
            // User is already signed in and should have a valid token.
            Log.debug("Authentication initiated...");
            // Token verification and recreation of the user with verified information only.
            FencePrincipal authenticatedUser = this.authenticationService.get().authenticate(notAuthenticatedUser);

            if (authenticatedUser != null) {
                // AUTHORIZATION
                Log.debugv("Authenticated user: {0}", authenticatedUser);
                Log.debug("Authorization initiated...");
                this.applyAuthorization(containerRequestContext, authenticatedUser);
                if (this.resourceInfo.getResourceMethod().isAnnotationPresent(Verify.class)) {
                    // VERIFY
                    Log.debug("Verification initiated...");
                    authenticatedUser = this.verify(containerRequestContext, authenticatedUser);
                    Log.debugv("Verified user: {0}", authenticatedUser);
                }
                this.authenticationEvent.get().fire(new FenceContext(authenticatedUser, true));
            } else {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.UNAUTHORIZED,
                    "Authentication failed."
                );
            }
        } else if (this.resourceInfo.getResourceMethod().isAnnotationPresent(SignUp.class)) {
            // SIGN UP
            Log.debug("Sign up initiated...");
            this.signUp(containerRequestContext, notAuthenticatedUser);
        } else if (this.resourceInfo.getResourceMethod().isAnnotationPresent(SignIn.class)) {
            // SIGN IN
            Log.debug("Sign in initiated...");
            this.signIn(containerRequestContext, notAuthenticatedUser);
        } else {
            Log.warnv(
                "Unauthenticated user tried to access matched resource [{0}::{1}].",
                this.resourceInfo.getResourceMethod().getDeclaringClass().getSimpleName(),
                this.resourceInfo.getResourceMethod().getName()
            );

            this.abortWith(
                containerRequestContext,
                Response.Status.UNAUTHORIZED,
                "Authentication required."
            );
        }
    }

    /**
     * Checks if an already authenticated user is authorized for the requested resource. Responsibility is delegated
     * to an implementation of {@link AuthorizationServiceApi} and if the authorization fails the request is aborted.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     * @param authenticatedUser Authenticated user.
     */
    private void applyAuthorization(final ContainerRequestContext containerRequestContext,
                                    final FencePrincipal authenticatedUser) {

        if (containerRequestContext.getUriInfo().getPath().contains("/auth/")) {
            if (this.authorizationService.get().isAuthorized(containerRequestContext.getUriInfo())) {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.OK,
                    "User is authorized to request the desired resource."
                );
            } else {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.FORBIDDEN,
                    "User is not authorized to request the desired resource."
                );
            }
        } else {
            if (this.authorizationService.get().isAuthorized(
                authenticatedUser,
                this.resourceInfo.getResourceMethod().getAnnotation(Fenced.class),
                this.resourceInfo.getResourceMethod(),
                containerRequestContext.getUriInfo(),
                containerRequestContext
            )) {
                // Authorization ok
                Log.debugv(
                    "Authenticated user is authorized for matched resource [{0}::{1}].",
                    this.resourceInfo.getResourceClass().getSimpleName(),
                    this.resourceInfo.getResourceMethod().getName()
                );
            } else {
                // Authorization failed
                Log.warnv(
                    "Authenticated user is not authorized for matched resource [{0}::{1}]",
                    this.resourceInfo.getResourceClass().getSimpleName(),
                    this.resourceInfo.getResourceMethod().getName()
                );
                this.abortWith(
                    containerRequestContext,
                    Response.Status.FORBIDDEN,
                    "User is not authorized to request the desired resource."
                );
            }
        }
    }

    /**
     * Uses an implementation {@link AuthenticationServiceApi} for the registration of new users and adds the created
     * {@link FencePrincipal} to the {@link FenceContext}.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     * @param clientUser {@link FencePrincipal} holding basic auth-relevant information.
     */
    private void signUp(final ContainerRequestContext containerRequestContext, final FencePrincipal clientUser) {
        try {
            FencePrincipal persistenceUser = this.authenticationService.get().signUp(clientUser);
            this.authenticationEvent.get().fire(new FenceContext(persistenceUser, true));
        } catch (PersistenceException e) {
            // TODO Name PersistenceUnits again and flush or move to PersistenceExceptionMapper
            if (Maple.toCausalChain(e).contains(SQLIntegrityConstraintViolationException.class)) {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.CONFLICT,
                    "Username or email already exists"
                );
            } else {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.BAD_REQUEST,
                    Maple.toRootCause(e).getMessage()
                );
            }
        } catch (Exception e) {
            this.abortWith(
                containerRequestContext,
                Response.Status.BAD_REQUEST,
                Maple.toRootCause(e).getMessage()
            );
        }
    }

    /**
     * Uses an implementation {@link AuthenticationServiceApi} to check the authenticity of clients and adds a verified
     * {@link FencePrincipal} with security token to the {@link FenceContext}. Identity is checked by means of username
     * and password.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     * @param clientUser {@link FencePrincipal} holding basic auth-relevant information.
     */
    private void signIn(final ContainerRequestContext containerRequestContext, final FencePrincipal clientUser) {
        if (StringUtils.isBlank(clientUser.getUsername())) {
            this.abortWith(
                containerRequestContext,
                Response.Status.BAD_REQUEST,
                "Blank username."
            );
        }

        if (StringUtils.isBlank(clientUser.getPassword())) {
            this.abortWith(
                containerRequestContext,
                Response.Status.BAD_REQUEST,
                "Blank password."
            );
        }

        try {
            FencePrincipal persistenceUser = this.authenticationService.get().signIn(clientUser);
            this.authenticationEvent.get().fire(new FenceContext(persistenceUser, true));
        } catch (NoResultException e) {
            this.abortWith(
                containerRequestContext,
                Response.Status.UNAUTHORIZED,
                "User does not exist."
            );
        }
    }

    private FencePrincipal verify(final ContainerRequestContext containerRequestContext,
                                  final FencePrincipal authenticatedUser) {
        FencePrincipal verifiedUser = null;
        FencePrincipal oidcUser = this.authenticationService.get().authenticate(
            this.createOidcPrincipal(containerRequestContext)
        );
        try {
             verifiedUser = this.authenticationService.get().verify(authenticatedUser, oidcUser);
        } catch (PersistenceException e) {
            if (Maple.toCausalChain(e).contains(SQLIntegrityConstraintViolationException.class)) {
                final String message = "User " + authenticatedUser.getId() +
                                       " is already verified or tried to use existing subject: " +
                                       Maple.toRootCause(e).getMessage();
                Log.warn(message);
                this.abortWith(
                    containerRequestContext,
                    Response.Status.BAD_REQUEST,
                    message
                );
            } else {
                this.abortWith(
                    containerRequestContext,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    Maple.toRootCause(e).getMessage()
                );
            }
        }
        return verifiedUser;
    }

    private FencePrincipal createPrincipal(final ContainerRequestContext containerRequestContext) {
        this.assertHeaders(containerRequestContext);

        Long id = null;
        String idHeader = containerRequestContext.getHeaderString(FenceHeaders.ID);

        if (StringUtils.isNotBlank(idHeader)) {
            id = Long.parseLong(idHeader);
        }

        return new Principal(
            id,
            containerRequestContext.getHeaderString(FenceHeaders.USERNAME),
            containerRequestContext.getHeaderString(FenceHeaders.PASSWORD),
            containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION),
            containerRequestContext.getHeaderString(FenceHeaders.EMAIL)
        );
    }

    private FencePrincipal createOidcPrincipal(final ContainerRequestContext containerRequestContext) {
        this.assertHeaders(containerRequestContext);
        FencePrincipal oidcPrincipal = null;
        if (containerRequestContext.getHeaders().containsKey(FenceHeaders.VERIFICATION)) {
            oidcPrincipal = new Principal();
            oidcPrincipal.setToken(containerRequestContext.getHeaderString(FenceHeaders.VERIFICATION));
        } else {
            this.abortWith(
                containerRequestContext,
                Response.Status.BAD_REQUEST,
                "Verification token is missing."
            );
        }

        return oidcPrincipal;
    }

    private void assertHeaders(final ContainerRequestContext containerRequestContext) {
        containerRequestContext.getHeaders().forEach((key, value) -> {
            if (value.size() > 1) {
                Log.warnv("Multiple entries for header '{0}'", key);
                if (key.startsWith(FenceHeaders.PREFIX) || key.equals(HttpHeaders.AUTHORIZATION)) {
                    this.abortWith(
                        containerRequestContext,
                        Response.Status.BAD_REQUEST,
                        "Duplicated authorization headers found."
                    );
                }
            }
        });
    }

    private void abortWith(final ContainerRequestContext containerRequestContext,
                           final Response.Status status,
                           final String message) {
        containerRequestContext.abortWith(
            Response.status(status)
                .entity(new ApiResponse(message))
                .build()
        );
    }
}
