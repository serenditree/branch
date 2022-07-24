package com.serenditree.branch.user.service.api;

import com.serenditree.branch.user.model.entities.User;
import com.serenditree.fence.authentication.service.api.AuthenticationAwareServiceApi;
import com.serenditree.fence.model.FenceResponse;

import java.util.List;

public interface UserServiceApi extends AuthenticationAwareServiceApi {
    User retrieveByUsername(String username);

    List<User> retrieveBySubstring(String substring);

    FenceResponse delete(Long id);
}
