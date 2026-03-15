package io.serenditree.fence.authorization.service.api;

import io.serenditree.fence.annotation.Fenced;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.fence.model.FenceRecordAssertion;
import io.serenditree.fence.model.api.FencePrincipal;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Method;

public interface AuthorizationService {
    boolean isAuthorized(FencePrincipal authenticatedUser,
                         Fenced fenced,
                         Method resourceMethod,
                         UriInfo uriInfo,
                         ContainerRequestContext containerRequestContext);

    boolean isAuthorized(UriInfo uriInfo);

    void assertThat(FenceRecordAssertion fenceRecordAssertion);

    void setAuthorizationRepository(AuthorizationRepository authorizationRepository);
}
