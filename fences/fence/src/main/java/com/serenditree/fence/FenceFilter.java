package com.serenditree.fence;

import com.serenditree.fence.annotation.*;
import com.serenditree.fence.authentication.service.api.AuthenticationServiceApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.root.etc.maple.Maple;
import com.serenditree.root.rest.transfer.ApiResponse;
import jakarta.enterprise.event.Event;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;

/**
 * Custom {@link ContainerRequestFilter} that handles authentication and authorization before a request reaches the
 * requested resource method. Security information is added to the {@link FenceContext}.
 */
public class FenceFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(FenceFilter.class.getName());

    private final ResourceInfo resourceInfo;

    private final AuthenticationServiceApi authenticationService;

    private final AuthorizationServiceApi authorizationService;

    private final Event<FenceContext> authenticationEvent;

    private final boolean forceHttps;

    private ContainerRequestContext containerRequestContext;

    private Method resourceMethod;

    private boolean secure;

    /**
     * Sets services for authentication and authorization which are injected dynamically in {@link FenceFeature}.
     *
     * @param resourceInfo          Contextual resource information.
     * @param authenticationService {@link AuthenticationServiceApi}
     * @param authorizationService  {@link AuthorizationServiceApi}
     * @param authenticationEvent   Used to publish successful authentications.
     * @param forceHttps            Flag that indicates if {@link FenceFilter} should enforce HTTPS.
     */
    public FenceFilter(ResourceInfo resourceInfo,
                       AuthenticationServiceApi authenticationService,
                       AuthorizationServiceApi authorizationService,
                       Event<FenceContext> authenticationEvent,
                       boolean forceHttps) {

        this.resourceInfo = resourceInfo;
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.authenticationEvent = authenticationEvent;
        this.forceHttps = forceHttps;
    }

    /**
     * Collects request information, enforces https connections and dispatches to auth-methods when preconditions are
     * met. If a resource method does not contain any authorization information, ie an annotation of type {@link Fenced}
     * or {@link Open} the request is aborted.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     */
    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        try {
            this.containerRequestContext = containerRequestContext;
            this.resourceMethod = this.resourceInfo.getResourceMethod();
            this.secure = this.containerRequestContext
                .getUriInfo()
                .getBaseUri()
                .getScheme()
                .equals("https");

            final String request = "Request [" + containerRequestContext.getUriInfo().getPath() + "]";
            LOGGER.fine(() -> request + " secure: " + secure);

            if (this.secure || !this.forceHttps) {
                // Assure that authorization is defined and that the client is authorized to request the
                // target resource!
                if (this.resourceMethod.isAnnotationPresent(Fenced.class)) {
                    LOGGER.fine(() -> request + " needs authorization or is for authentication purposes.");
                    this.applyAuthenticationAndAuthorization();
                } else if (!this.resourceMethod.isAnnotationPresent(Open.class)) {
                    // To avoid unintentional access, (new) resources without auth-annotations are blocked per default.
                    LOGGER.severe(() -> request + " has no authorization information.");
                    this.abortWith(Response.Status.FORBIDDEN, request + " is not yet available.");
                }
            } else {
                LOGGER.severe(() -> request + " over an insecure connection.");
                this.abortWith(Response.Status.BAD_REQUEST, request + " requires a secure connection.");
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.severe(() ->
                              "Unexpected error during authentication and authorization. Exception: " +
                              e.getMessage() +
                              "; Root cause: " +
                              Maple.toRootCause(e).getMessage()
            );

            this.abortWith(
                Response.Status.INTERNAL_SERVER_ERROR,
                "Unexpected error during authentication and authorization."
            );
        }
    }

    /**
     * Dispatches to sign up/in methods or methods for authentication and authorization.
     */
    private void applyAuthenticationAndAuthorization() {

        FencePrincipal notAuthenticatedUser = this.createPrincipal(this.containerRequestContext.getHeaders());

        if (StringUtils.isNotBlank(notAuthenticatedUser.getToken())) {
            // AUTHENTICATION
            // User is already signed in and should have a valid token.
            LOGGER.fine("Authentication initiated...");
            // Token verification and recreation of the user with verified information only.
            final FencePrincipal authenticatedUser = this.authenticationService.authenticate(notAuthenticatedUser);

            if (authenticatedUser != null) {
                // AUTHORIZATION
                LOGGER.fine(() -> "Authenticated user:\n" + Maple.prettyJson(authenticatedUser));
                LOGGER.fine("Authorization initiated...");
                this.applyAuthorization(authenticatedUser);
                if (this.resourceMethod.isAnnotationPresent(Verify.class)) {
                    // VERIFY
                    LOGGER.fine("Verification initiated...");
                    this.verify(authenticatedUser);
                    LOGGER.fine(() -> "Verified user:\n" + Maple.prettyJson(authenticatedUser));
                }
                this.authenticationEvent.fire(new FenceContext(authenticatedUser, this.secure));
            } else {
                this.abortWith(Response.Status.UNAUTHORIZED, "Authentication failed.");
            }
        } else if (this.resourceMethod.isAnnotationPresent(SignUp.class)) {
            // SIGN UP
            LOGGER.fine("Sign up initiated...");
            this.signUp(notAuthenticatedUser);
        } else if (this.resourceMethod.isAnnotationPresent(SignIn.class)) {
            // SIGN IN
            LOGGER.fine("Sign in initiated...");
            this.signIn(notAuthenticatedUser);
        } else {
            LOGGER.warning(() ->
                               "Unauthenticated user tried to access matched resource [" +
                               this.resourceMethod.getDeclaringClass().getSimpleName() + "::" +
                               this.resourceMethod.getName() + "]."
            );

            this.abortWith(Response.Status.UNAUTHORIZED, "Authentication required.");
        }
    }

    /**
     * Checks if an already authenticated user is authorized for the requested resource. Responsibility is delegated
     * to an implementation of {@link AuthorizationServiceApi} and if the authorization fails the request is aborted.
     *
     * @param authenticatedUser Authenticated user.
     */
    private void applyAuthorization(final FencePrincipal authenticatedUser) {

        if (this.containerRequestContext.getUriInfo().getPath().contains("/auth/")) {
            if (this.authorizationService.isAuthorized(this.containerRequestContext.getUriInfo())) {
                this.abortWith(Response.Status.OK, "User is authorized to request the desired resource.");
            } else {
                this.abortWith(Response.Status.FORBIDDEN, "User is not authorized to request the desired resource.");
            }
        } else {
            if (this.authorizationService.isAuthorized(
                authenticatedUser,
                this.resourceMethod.getAnnotation(Fenced.class),
                this.resourceMethod,
                this.containerRequestContext.getUriInfo(),
                this.containerRequestContext
            )) {
                // Authorization ok
                LOGGER.fine(() ->
                                "Authenticated user is authorized for matched resource [" +
                                this.resourceMethod.getDeclaringClass().getSimpleName() + "::" +
                                this.resourceMethod.getName() + "]."
                );
            } else {
                // Authorization failed
                LOGGER.warning(() ->
                                   "Authenticated user is not authorized for matched resource [" +
                                   this.resourceMethod.getDeclaringClass().getSimpleName() + "::" +
                                   this.resourceMethod.getName() + "]."
                );
                this.abortWith(Response.Status.FORBIDDEN, "User is not authorized to request the desired resource.");
            }
        }
    }

    /**
     * Uses an implementation {@link AuthenticationServiceApi} for the registration of new users and adds the created
     * {@link FencePrincipal} to the {@link FenceContext}.
     *
     * @param clientUser {@link FencePrincipal} holding basic auth-relevant information.
     */
    private void signUp(final FencePrincipal clientUser) {
        try {
            FencePrincipal persistenceUser = this.authenticationService.signUp(clientUser);
            this.authenticationEvent.fire(new FenceContext(persistenceUser, this.secure));
        } catch (PersistenceException e) {
            // TODO Name PersistenceUnits again and flush or move to PersistenceExceptionMapper
            if (Maple.toCausalChain(e).contains(SQLIntegrityConstraintViolationException.class)) {
                this.abortWith(Response.Status.CONFLICT, "Username or email already exists");
            } else {
                this.abortWith(Response.Status.BAD_REQUEST, Maple.toRootCause(e).getMessage());
            }
        } catch (Exception e) {
            this.abortWith(Response.Status.BAD_REQUEST, Maple.toRootCause(e).getMessage());
        }
    }

    /**
     * Uses an implementation {@link AuthenticationServiceApi} to check the authenticity of clients and adds a verified
     * {@link FencePrincipal} with security token to the {@link FenceContext}. Identity is checked by means of username
     * and password.
     *
     * @param clientUser {@link FencePrincipal} holding basic auth-relevant information.
     */
    private void signIn(final FencePrincipal clientUser) {
        if (StringUtils.isBlank(clientUser.getUsername())) {
            this.abortWith(Response.Status.BAD_REQUEST, "Blank username.");
        }

        if (StringUtils.isBlank(clientUser.getPassword())) {
            this.abortWith(Response.Status.BAD_REQUEST, "Blank password.");
        }

        try {
            FencePrincipal persistenceUser = this.authenticationService.signIn(clientUser);
            this.authenticationEvent.fire(new FenceContext(persistenceUser, this.secure));
        } catch (NoResultException e) {
            this.abortWith(Response.Status.UNAUTHORIZED, "User does not exist.");
        }
    }

    private void verify(final FencePrincipal authenticatedUser) {
        FencePrincipal oidcUser = this.authenticationService.authenticate(
            this.createOidcPrincipal(this.containerRequestContext.getHeaders())
        );
        try {
            this.authenticationService.verify(authenticatedUser, oidcUser);
        } catch (PersistenceException e) {
            // TODO Name PersistenceUnits again and flush or move to PersistenceExceptionMapper
            if (Maple.toCausalChain(e).contains(SQLIntegrityConstraintViolationException.class)) {
                final String message = "User " + authenticatedUser.getId() +
                                       " is already verified or tried to use existing subject: " +
                                       Maple.toRootCause(e).getMessage();
                LOGGER.severe(message);
                this.abortWith(Response.Status.BAD_REQUEST, message);
            } else {
                this.abortWith(Response.Status.INTERNAL_SERVER_ERROR, Maple.toRootCause(e).getMessage());
            }
        }
    }

    /**
     * Creates an instance of {@link FencePrincipal} by collecting header values.
     *
     * @param headers Request headers containing auth-information.
     * @return Instance of {@link FencePrincipal}
     */
    private FencePrincipal createPrincipal(final MultivaluedMap<String, String> headers) {
        this.assertHeaders(headers);

        Long id = null;
        String idHeader = headers.getFirst(FenceHeaders.ID);

        if (StringUtils.isNotBlank(idHeader)) {
            id = Long.parseLong(idHeader);
        }

        return new Principal(
            id,
            headers.getFirst(FenceHeaders.USERNAME),
            headers.getFirst(FenceHeaders.PASSWORD),
            headers.getFirst(HttpHeaders.AUTHORIZATION),
            headers.getFirst(FenceHeaders.EMAIL)
        );
    }

    private FencePrincipal createOidcPrincipal(final MultivaluedMap<String, String> headers) {
        this.assertHeaders(headers);
        FencePrincipal oidcPrincipal = null;
        if (headers.containsKey(FenceHeaders.VERIFICATION)) {
            oidcPrincipal = new Principal();
            oidcPrincipal.setToken(headers.getFirst(FenceHeaders.VERIFICATION));
        } else {
            this.abortWith(Response.Status.BAD_REQUEST, "Verification token is missing.");
        }

        return oidcPrincipal;
    }

    private void assertHeaders(final MultivaluedMap<String, String> headers) {
        headers.forEach((key, value) -> {
            if (value.size() > 1) {
                LOGGER.warning(() -> "Multiple entries for header '" + key + "'");
                if (key.startsWith(FenceHeaders.PREFIX) || key.equals(HttpHeaders.AUTHORIZATION)) {
                    this.abortWith(Response.Status.BAD_REQUEST, "Duplicated authorization headers found.");
                }
            }
        });
    }

    /**
     * Aborts the current request.
     *
     * @param status  HTTP status.
     * @param message Informative message.
     */
    private void abortWith(final Response.Status status, final String message) {
        this.containerRequestContext.abortWith(
            Response.status(status)
                .entity(new ApiResponse(message))
                .build()
        );
    }
}
