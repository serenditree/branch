package com.serenditree.fence.authorization.service;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.authorization.service.api.PolicyEnforcerApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
import io.restassured.path.json.JsonPath;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks if a request is allowed to proceed based on role- and action-base-authorization. Optional policies
 * inspecting the request body are applied.
 * TODO It is not yet assured that api implementations are using id information of the request body,
 * TODO bypassing the id assertion based on path parameters.
 */
@Dependent
public class AuthorizationService implements AuthorizationServiceApi {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationService.class.getName());

    @ConfigProperty(name = "serenditree.fence.policies.enabled", defaultValue = "false")
    boolean isPolicyEnforcerEnabled;

    private ContainerRequestContext containerRequestContext;

    private AuthorizationRepositoryApi authorizationRepository;

    private PolicyEnforcerApi policyEnforcer;

    private Instance<PolicyEnforcerApi> policyEnforcerInstance;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Starting point for the verification if the given user is authorized to access the given resource.
     *
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource.
     */
    @Override
    public boolean isAuthorized(FencePrincipal authenticatedUser,
                                Fenced fenced,
                                Method resourceMethod,
                                UriInfo uriInfo,
                                ContainerRequestContext containerRequestContext) {
        this.containerRequestContext = containerRequestContext;

        // Role based authorization.
        boolean authorized = this.rolesAllowed(authenticatedUser, fenced);

        // Request body policy enforcement.
        if (this.isPolicyEnforcerEnabled && authorized && fenced.policies().length > 0) {
            LOGGER.fine("Policy enforcement initiated...");

            authorized = this.applyPolicies(fenced.policies(), authenticatedUser.getId().toString());
        }

        // Action based authorization.
        if (authorized && fenced.actionBased()) {
            LOGGER.fine("Action based authorization initiated...");

            String action;
            if (fenced.recordType() == FenceActionType.METHOD) {
                action = resourceMethod.getName();
            } else {
                action = fenced.recordType().name();
            }

            String targetId = uriInfo.getPathParameters(true).getFirst("id");
            if (targetId == null) {
                throw new SecurityException("Action based authorization requires id information in path.");
            }

            authorized = this.isAuthorizedAssertion(
                authenticatedUser.getId().toString(),
                targetId,
                action,
                fenced.recordRequired(),
                fenced.recordCount()
            );
        }

        return authorized;
    }

    /**
     * Method to check authorization without touching the target resource. Don't use for real authorization checks!
     * ID is not verified.
     *
     * @param uriInfo Needed for the extraction of the values annotated with {@link javax.ws.rs.PathParam}.
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource.
     * @apiNote Don't use for real authorization checks! ID is not verified.
     */
    @Override
    public boolean isAuthorized(UriInfo uriInfo) {
        LOGGER.fine("Simulating authorization...");
        MultivaluedMap<String, String> params = uriInfo.getPathParameters(true);

        return this.isAuthorizedAssertion(
            params.getFirst("userId"),
            params.getFirst("entityId"),
            params.getFirst("action"),
            false,
            0
        );
    }

    /**
     * Asserts that the given {@link FenceRecordAssertion} succeeds. If the assertion fails a
     * {@link ForbiddenException} is thrown.
     *
     * @param fenceRecordAssertion Information to assert.
     */
    @Override
    public void assertThat(FenceRecordAssertion fenceRecordAssertion) {
        if (!this.isAuthorizedAssertion(fenceRecordAssertion)) {
            throw new ForbiddenException("Assertion failed.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks the required security assertion information against available {@link FenceRecord}s.
     *
     * @param userId         ID of the authenticated user.
     * @param entityId       ID of the targeted entity.
     * @param action         Requested action to perform.
     * @param recordRequired Flag that indicates if the presence of absence of the defined record is required.
     * @param recordCount    Checks the number of found records. For example an action might be allowed if 2 not-expired
     *                       records are found.
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource.
     */
    private boolean isAuthorizedAssertion(
        final String userId,
        final String entityId,
        final String action,
        final boolean recordRequired,
        final int recordCount) {

        LOGGER.fine(() ->
                        "Authorization check: " +
                        "userId[" + userId + "], " +
                        "entityId[" + entityId + "], " +
                        "action[" + action + "], " +
                        "recordRequired[" + recordRequired + "], " +
                        "recordCount[" + recordCount + "]"
        );

        List<FenceRecord> fenceRecords = this.authorizationRepository.retrieveFenceRecords(userId, entityId, action);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("FenceRecords: " + fenceRecords.size());
            fenceRecords.forEach(fenceRecord -> LOGGER.fine(fenceRecord.toString()));
        }

        boolean authorized = false;

        if (!fenceRecords.isEmpty()) {
            // Ordered by expiration date at query level.
            FenceRecord fenceRecord = fenceRecords.get(0);
            if (recordRequired) {
                authorized = fenceRecord.getExpiration() == null ||
                             LocalDateTime.now().isBefore(fenceRecord.getExpiration());
            } else {
                authorized = fenceRecord.getExpiration() != null &&
                             LocalDateTime.now().isAfter(fenceRecord.getExpiration());
            }
        } else if (!recordRequired) {
            authorized = true;
        }

        return authorized;
    }

    /**
     * Checks the required security assertion information against available {@link FenceRecord}s. The assertion
     * information is encapsulated in a {@link FenceRecordAssertion}.
     *
     * @param fenceRecordAssertion {@link FenceRecordAssertion} containing security assertion information.
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource.
     * @see AuthorizationService#isAuthorizedAssertion(String, String, String, boolean, int)
     */
    private boolean isAuthorizedAssertion(FenceRecordAssertion fenceRecordAssertion) {
        return this.isAuthorizedAssertion(
            fenceRecordAssertion.getUserId(),
            fenceRecordAssertion.getEntityId(),
            fenceRecordAssertion.getActionType().name(),
            fenceRecordAssertion.isRecordRequired(),
            fenceRecordAssertion.getRecordCount()
        );
    }

    /**
     * Verifies if the user is in a role that authorizes her or him to access a certain resource.
     *
     * @param authenticatedUser Authenticated user who claims authorization.
     * @param fenced            Information about the resource the user claims to be authorized for.
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource based on its roles.
     */
    private boolean rolesAllowed(FencePrincipal authenticatedUser, Fenced fenced) {
        final boolean authorized = Arrays
            .stream(fenced.rolesAllowed())
            .anyMatch(authenticatedUser::isInRole);

        LOGGER.fine(() -> "Authorized based on role: " + authorized);

        return authorized;
    }

    /**
     * Enforces the given policies - optionally against the provided user.
     *
     * @param policies Policies to apply.
     * @param userId   Id of the user.
     * @return Boolean flag to indicate if all policies passed.
     */
    private boolean applyPolicies(final String[] policies, final String userId) {
        boolean authorized;

        try (final InputStream entityStream = this.containerRequestContext.getEntityStream()) {
            final String body = IOUtils.toString(entityStream, Charset.defaultCharset());
            final JsonPath json = JsonPath.from(body);

            LOGGER.fine(() -> "Starting enforcement of " + policies.length + " policies: " + Arrays.toString(policies));
            LOGGER.fine(() -> "Request body: " + body);
            LOGGER.fine(() -> "User id: " + userId);

            authorized = this.policyEnforcer
                .getPolicies(policies)
                .parallelStream()
                .map(fencePolicy -> fencePolicy.apply(json, userId))
                .filter(Optional::isPresent)
                .map(assertion -> this.isAuthorizedAssertion(assertion.get()))
                .reduce(true, (acc, curr) -> acc && curr);

            this.containerRequestContext.setEntityStream(IOUtils.toInputStream(body, Charset.defaultCharset()));
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read entity stream.", e);
        }

        return authorized;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    @Inject
    public void setPolicyEnforcerInstance(Instance<PolicyEnforcerApi> policyEnforcerInstance) {
        this.policyEnforcerInstance = policyEnforcerInstance;
    }

    /**
     * Sets an implementation of {@link PolicyEnforcerApi} - if available.
     */
    @PostConstruct
    void init() {
        if (this.policyEnforcerInstance.isAmbiguous()) {
            throw new IllegalStateException(
                "Found more than one implementation of " + PolicyEnforcerApi.class.getName()
            );
        } else if (this.policyEnforcerInstance.isResolvable()) {
            this.policyEnforcer = this.policyEnforcerInstance.get();
        } else {
            this.policyEnforcer = null;
            LOGGER.fine("No policy enforcer found.");
        }
    }
}
