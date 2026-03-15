package io.serenditree.fence.authentication.service;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.serenditree.fence.authentication.service.api.*;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.fence.model.FenceRecord;
import io.serenditree.fence.model.Principal;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.util.oak.OakHtml;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * Service for the initial and/or repeated authentication of clients. If the context provides an implementation of
 * {@link AuthenticationAwareService} it is used for sign-in and sign-up of clients.
 */
@Startup
@RequestScoped
public class JoseOrmAuthenticationService implements AuthenticationService {

    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final VerificationService verificationService;
    private final AuthorizationRepository authorizationRepository;
    private final Instance<AuthenticationAwareService> authenticationAwareServiceInstance;

    private AuthenticationAwareService authenticationAwareService;

    @Inject
    public JoseOrmAuthenticationService(
        TokenService tokenService,
        PasswordService passwordService,
        VerificationService verificationService,
        AuthorizationRepository authorizationRepository,
        Instance<AuthenticationAwareService> authenticationAwareServiceInstance
    ) {
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.verificationService = verificationService;
        this.authorizationRepository = authorizationRepository;
        this.authenticationAwareServiceInstance = authenticationAwareServiceInstance;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Creates a new user using an implementation of {@link AuthorizationRepository}. A {@link FenceRecord} is
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
     * Authenticates a user using an implementation of {@link AuthenticationAwareService} and
     * {@link PasswordService}.
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

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void assertAuthenticationAwareness() {
        if (this.authenticationAwareServiceInstance.isUnsatisfied()) {
            throw new UnsupportedOperationException("Unsupported usage of token-only authentication service.");
        }
    }

    /**
     * Checks if the context provides an implementation of {@link AuthenticationAwareService} and sets it if
     * available.
     */
    @PostConstruct
    void init() {
        if (this.authenticationAwareServiceInstance.isAmbiguous()) {
            throw new IllegalStateException(
                "Found more than one implementation of " + AuthenticationAwareService.class.getName()
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
