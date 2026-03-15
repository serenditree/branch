package io.serenditree.fence.authorization.service;

import io.serenditree.fence.annotation.Fenced;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.fence.authorization.service.api.AuthorizationService;
import io.serenditree.fence.model.FenceRecord;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.FenceActionType;
import io.serenditree.fence.model.enums.RoleType;
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
class OrmAuthorizationServiceTest {

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    AuthorizationRepository authorizationRepository;

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        AuthorizationService authorizationService = new OrmAuthorizationService();
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

        AuthorizationService authorizationService = new OrmAuthorizationService();
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

        AuthorizationService authorizationService = new OrmAuthorizationService();
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
