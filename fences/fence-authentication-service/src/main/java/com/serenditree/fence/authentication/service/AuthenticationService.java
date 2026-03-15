package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.*;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.util.oak.OakHtml;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * Service for the initial and/or repeated authentication of clients. If the context provides an implementation of
 * {@link AuthenticationAwareServiceApi} it is used for sign-in and sign-up of clients.
 */
@Startup
@RequestScoped
public class AuthenticationService implements AuthenticationServiceApi {

    private TokenServiceApi tokenService;

    private PasswordServiceApi passwordService;

    private VerificationServiceApi verificationService;

    private AuthorizationRepositoryApi authorizationRepository;

    private Instance<AuthenticationAwareServiceApi> authenticationAwareServiceInstance;

    private AuthenticationAwareServiceApi authenticationAwareService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Uses the token of an authenticated user for re-authentication.
     *
     * @param clientUser User that has been initially authenticated.
     * @return {@link FencePrincipal}
     */
    @Override
    public FencePrincipal authenticate(final FencePrincipal clientUser) {
        return this.tokenService.authenticate(clientUser.getToken());
    }

    /**
     * Creates a new user using an implementation of {@link AuthorizationRepositoryApi}. A {@link FenceRecord} is
     * created to authorize the user to manage the account.
     *
     * @param clientUser Unauthenticated, new user.
     * @return Persisted, authenticated user.
     */
    @Override
    @Transactional
    public FencePrincipal signUp(final FencePrincipal clientUser) {
        this.assertAuthenticationAwareness();
        OakHtml.sanitize(clientUser.getUsername());

        FencePrincipal persistenceUser = this.authenticationAwareService.signUp(clientUser);
        persistenceUser.setToken(this.tokenService.buildToken(persistenceUser));

        this.authorizationRepository.createFenceRecord(new FenceRecord(persistenceUser));

        return persistenceUser;
    }

    /**
     * Authenticates a user using an implementation of {@link AuthenticationAwareServiceApi} and
     * {@link PasswordServiceApi}.
     *
     * @param clientUser Unauthenticated user.
     * @return Authenticated user with information from persistence.
     */
    @Override
    public FencePrincipal signIn(final FencePrincipal clientUser) {
        this.assertAuthenticationAwareness();

        // Retrieve stored user information!
        Log.debugv("Retrieving user {0} from persistence...", clientUser.getUsername());
        FencePrincipal persistenceUser = this.authenticationAwareService
            .retrievePrincipalByUsername(clientUser.getUsername());

        // Check password!
        Log.debug("Checking password...");
        if (this.passwordService.verify(persistenceUser.getPassword(), clientUser.getPassword())) {
            Log.debug("User with corresponding password found. Creating token...");
            persistenceUser.setToken(this.tokenService.buildToken(persistenceUser));
        } else {
            Log.warnv("User found but provided password is wrong: {0}", clientUser);
            // TODO remember login attempt
            throw new BadRequestException("Wrong password.");
        }

        return persistenceUser;
    }

    @Override
    @Transactional
    public FencePrincipal verify(final FencePrincipal principal, final FencePrincipal oidcPrincipal) {
        this.assertAuthenticationAwareness();
        if (principal.getId().equals(oidcPrincipal.getId())) {
            this.verificationService.createFenceIdRecord(oidcPrincipal.getUsername());
            this.authenticationAwareService.verify(principal);
        } else {
            final String message = "User " + principal.getId() + " tried to use a stolen OIDC verification token.";
            Log.warn(message);
            throw new NotAuthorizedException(message);
        }
        FencePrincipal updatedPrincipal = new Principal(principal);
        updatedPrincipal.addRole(RoleType.HUMAN);
        updatedPrincipal.setToken(this.tokenService.buildToken(updatedPrincipal));

        return updatedPrincipal;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void assertAuthenticationAwareness() {
        if (this.authenticationAwareServiceInstance.isUnsatisfied()) {
            throw new UnsupportedOperationException("Unsupported usage of token-only authentication service.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setTokenService(TokenServiceApi tokenService) {
        this.tokenService = tokenService;
    }

    @Inject
    public void setPasswordService(PasswordServiceApi passwordService) {
        this.passwordService = passwordService;
    }

    @Inject
    public void setVerificationService(VerificationServiceApi verificationService) {
        this.verificationService = verificationService;
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    @Inject
    public void setAuthenticationAwareServiceInstance(
        Instance<AuthenticationAwareServiceApi> authenticationAwareServiceInstance) {
        this.authenticationAwareServiceInstance = authenticationAwareServiceInstance;
    }

    /**
     * Checks if the context provides an implementation of {@link AuthenticationAwareServiceApi} and sets it if
     * available.
     */
    @PostConstruct
    void init() {
        if (this.authenticationAwareServiceInstance.isAmbiguous()) {
            throw new IllegalStateException(
                "Found more than one implementation of " + AuthenticationAwareServiceApi.class.getName()
            );

        } else if (this.authenticationAwareServiceInstance.isResolvable()) {
            this.authenticationAwareService = this.authenticationAwareServiceInstance.get();
            Log.debugv(
                "Constructed {0} with {1}",
                this.getClass().getSimpleName(),
                this.authenticationAwareService.getClass().getName()
            );
        } else {
            Log.debugv("Constructed token-only {0}", this.getClass().getSimpleName());
        }
    }
}
