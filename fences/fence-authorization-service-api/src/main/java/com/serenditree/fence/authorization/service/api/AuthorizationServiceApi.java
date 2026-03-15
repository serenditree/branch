package com.serenditree.fence.authorization.service.api;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.api.FencePrincipal;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Method;

public interface AuthorizationServiceApi {
    boolean isAuthorized(FencePrincipal authenticatedUser,
                         Fenced fenced,
                         Method resourceMethod,
                         UriInfo uriInfo,
                         ContainerRequestContext containerRequestContext);

    boolean isAuthorized(UriInfo uriInfo);

    void assertThat(FenceRecordAssertion fenceRecordAssertion);

    void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository);
}
