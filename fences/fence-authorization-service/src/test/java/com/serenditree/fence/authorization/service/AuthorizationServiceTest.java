package com.serenditree.fence.authorization.service;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.authorization.service.api.AuthorizationServiceApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class AuthorizationServiceTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Mock
    FencePrincipal authenticatedUser;

    @Mock
    Fenced fenced;

    @Mock
    UriInfo uriInfo;

    @Mock
    ContainerRequestContext containerRequestContext;

    @Mock
    MultivaluedMap<String, String> parameters;

    @Mock
    FenceRecord fenceRecord;

    @Mock
    AuthorizationRepositoryApi authorizationRepository;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Authorized because in role and the requirement that a valid {@link FenceRecord} is present is met.
     */
    @Test
    void isAuthorizedActionBasedRecordRequiredInRoleTest() {
        when(this.authenticatedUser.isInRole(any())).thenReturn(true);

        when(this.fenced.actionBased()).thenReturn(true);
        when(this.fenced.recordType()).thenReturn(FenceActionType.CRUD);
        when(this.fenced.recordRequired()).thenReturn(true);
        when(this.fenced.rolesAllowed()).thenReturn(new RoleType[]{RoleType.USER});

        when(this.parameters.getFirst("id")).thenReturn("1");
        when(this.uriInfo.getPathParameters(anyBoolean())).thenReturn(this.parameters);


        List<FenceRecord> records = Collections.singletonList(this.fenceRecord);
        when(this.authorizationRepository.retrieveFenceRecords(anyString(), anyString(), anyString()))
            .thenReturn(records);

        AuthorizationServiceApi authorizationService = new AuthorizationService();
        authorizationService.setAuthorizationRepository(this.authorizationRepository);

        boolean authorized = authorizationService.isAuthorized(
            this.authenticatedUser,
            this.fenced,
            this.getClass().getMethods()[0],
            this.uriInfo,
            this.containerRequestContext
        );

        assertTrue(authorized);
    }

    /**
     * Authorized because in role and the requirement that no {@link FenceRecord} is present is met.
     */
    @Test
    void isAuthorizedActionBasedNoRecordRequiredInRoleTest() {
        when(this.authenticatedUser.isInRole(any())).thenReturn(true);

        when(this.fenced.actionBased()).thenReturn(true);
        when(this.fenced.recordType()).thenReturn(FenceActionType.METHOD);
        when(this.fenced.recordRequired()).thenReturn(false);
        when(this.fenced.rolesAllowed()).thenReturn(new RoleType[]{RoleType.USER});

        when(this.parameters.getFirst("id")).thenReturn("1");
        when(this.uriInfo.getPathParameters(anyBoolean())).thenReturn(this.parameters);

        when(this.authorizationRepository.retrieveFenceRecords(anyString(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        AuthorizationServiceApi authorizationService = new AuthorizationService();
        authorizationService.setAuthorizationRepository(this.authorizationRepository);

        boolean authorized = authorizationService.isAuthorized(
            this.authenticatedUser,
            this.fenced,
            this.getClass().getMethods()[0],
            this.uriInfo,
            this.containerRequestContext
        );

        assertTrue(authorized);
    }

    /**
     * Users are not authorized because they are not in the required role.
     */
    @Test
    void isNotAuthorizedNotInRoleTest() {
        when(this.authenticatedUser.isInRole(any())).thenReturn(false);

        when(this.fenced.rolesAllowed()).thenReturn(new RoleType[]{RoleType.USER});

        AuthorizationServiceApi authorizationService = new AuthorizationService();
        authorizationService.setAuthorizationRepository(this.authorizationRepository);

        boolean authorized = authorizationService.isAuthorized(
            this.authenticatedUser,
            this.fenced,
            this.getClass().getMethods()[0],
            this.uriInfo,
            this.containerRequestContext
        );

        assertFalse(authorized);
    }
}
