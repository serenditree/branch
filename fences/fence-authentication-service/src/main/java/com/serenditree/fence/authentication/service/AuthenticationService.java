package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.*;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.util.logging.Logger;

/**
 * Service for the initial and/or repeated authentication of clients. If the context provides an implementation of
 * {@link AuthenticationAwareServiceApi} it is used for sign-in and sign-up of clients.
 */
@Dependent
public class AuthenticationService implements AuthenticationServiceApi {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

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
        FencePrincipal persistenceUser = this.authenticationAwareService
            .retrievePrincipalByUsername(clientUser.getUsername());

        // Check password!
        if (this.passwordService.verify(persistenceUser.getPassword(), clientUser.getPassword())) {
            LOGGER.fine("User with corresponding password found. Creating token...");
            persistenceUser.setToken(this.tokenService.buildToken(persistenceUser));
        } else {
            LOGGER.warning(() -> "User found but provided password is wrong: " + clientUser);
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
            LOGGER.severe(message);
            throw new NotAuthorizedException(message);
        }
        principal.addRole(RoleType.HUMAN);
        principal.setToken(this.tokenService.buildToken(principal));

        return principal;
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

            LOGGER.fine(() ->
                            "Constructed AuthenticationService with " + AuthenticationAwareServiceApi.class.getName()
            );

        } else {
            LOGGER.fine("Constructed token-only AuthenticationService");
        }
    }
}
