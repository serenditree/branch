package com.serenditree.branch.user.repository.api;

import com.serenditree.branch.user.model.entities.User;
import com.serenditree.fence.model.api.FencePrincipal;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;

public interface UserRepositoryApi extends PanacheRepository<User> {
    User create(User user);

    void verify(FencePrincipal principal);

    User retrieveByUsername(String username);

    List<User> retrieveBySubstring(String substring);
}
