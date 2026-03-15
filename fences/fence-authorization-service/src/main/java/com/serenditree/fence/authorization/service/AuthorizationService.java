package com.serenditree.fence.authorization.service;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.authorization.service.api.PolicyEnforcerApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.restassured.path.json.JsonPath;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Checks if a request is allowed to proceed based on role- and action-base-authorization. Optional policies
 * inspecting the request body are applied.
 */
@Startup
@RequestScoped
public class AuthorizationService implements AuthorizationServiceApi {

    @ConfigProperty(name = "serenditree.fence.policies.enabled", defaultValue = "false")
    boolean isPolicyEnforcerEnabled;

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
        // Role based authorization.
        boolean authorized = this.rolesAllowed(authenticatedUser, fenced);

        // Request body policy enforcement.
        if (this.isPolicyEnforcerEnabled && authorized && fenced.policies().length > 0) {
            Log.debug("Policy enforcement initiated...");

            authorized = this.applyPolicies(
                containerRequestContext,
                fenced.policies(),
                authenticatedUser.getId().toString()
            );
        }

        // Action based authorization.
        if (authorized && fenced.actionBased()) {
            Log.debug("Action based authorization initiated...");

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
     * @param uriInfo Needed for the extraction of the values annotated with {@link jakarta.ws.rs.PathParam}.
     * @return Boolean flag that indicates whether a user is authorized to access a certain resource.
     * @apiNote Don't use for real authorization checks! ID is not verified.
     */
    @Override
    public boolean isAuthorized(UriInfo uriInfo) {
        Log.debug("Simulating authorization...");
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

        Log.debugv(
            "Authorization check: userId[{0}], entityId[{1}], action[{2}], recordRequired[{3}], recordCount[{4}]",
            userId,
            entityId,
            action,
            recordRequired,
            recordCount
        );

        List<FenceRecord> fenceRecords = this.authorizationRepository.retrieveFenceRecords(userId, entityId, action);

        if (Log.isDebugEnabled()) {
            Log.debugv("FenceRecords: {0}", fenceRecords.size());
            fenceRecords.forEach(fenceRecord -> Log.debug(fenceRecord.toString()));
        }

        boolean authorized = false;

        if (!fenceRecords.isEmpty()) {
            // Ordered by expiration date at query level.
            FenceRecord fenceRecord = fenceRecords.getFirst();
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

        Log.debugv("Authorized based on role: {0}", authorized);

        return authorized;
    }

    /**
     * Enforces the given policies - optionally against the provided user.
     *
     * @param containerRequestContext {@link ContainerRequestContext}
     * @param policies Policies to apply.
     * @param userId   ID of the user.
     * @return Boolean flag to indicate if all policies passed.
     */
    private boolean applyPolicies(final ContainerRequestContext containerRequestContext,
                                  final String[] policies,
                                  final String userId) {
        boolean authorized;

        try (final InputStream entityStream = containerRequestContext.getEntityStream()) {
            final String body = IOUtils.toString(entityStream, Charset.defaultCharset());
            final JsonPath json = JsonPath.from(body);

            Log.debugv("Starting enforcement of {0} policies: {1}", policies.length, Arrays.toString(policies));
            Log.debugv("Request body: {0}", body);
            Log.debugv("User id: {0}", userId);

            authorized = this.policyEnforcer
                .getPolicies(policies)
                .parallelStream()
                .map(fencePolicy -> fencePolicy.apply(json, userId))
                .filter(Optional::isPresent)
                .map(assertion -> this.isAuthorizedAssertion(assertion.get()))
                .reduce(true, (acc, curr) -> acc && curr);

            containerRequestContext.setEntityStream(IOUtils.toInputStream(body, Charset.defaultCharset()));
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
        Log.debugv("Constructed {0}", this.getClass().getSimpleName());
        if (this.policyEnforcerInstance.isAmbiguous()) {
            throw new IllegalStateException(
                "Found more than one implementation of " + PolicyEnforcerApi.class.getName()
            );
        } else if (this.policyEnforcerInstance.isResolvable()) {
            this.policyEnforcer = this.policyEnforcerInstance.get();
            Log.debugv("Registered PolicyEnforcer {0}", this.policyEnforcer.getClass().getName());
        } else {
            this.policyEnforcer = null;
            Log.debug("No policy enforcer found");
        }
    }
}
