package io.serenditree.branch.user.service.api;

import io.serenditree.branch.user.model.entities.User;
import io.serenditree.fence.authentication.service.api.AuthenticationAwareServiceApi;
import io.serenditree.fence.model.FenceResponse;

import java.util.List;

public interface UserServiceApi extends AuthenticationAwareServiceApi {
    User retrieveByUsername(String username);

    List<User> retrieveBySubstring(String substring);

    FenceResponse delete(Long id, boolean includeContributions);
}
