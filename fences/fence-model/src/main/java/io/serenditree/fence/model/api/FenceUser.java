package io.serenditree.fence.model.api;

import io.serenditree.fence.model.enums.RoleType;

import java.util.Set;

public interface FenceUser<R extends FenceRole> {
    Long getId();

    void setId(Long id);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getEmail();

    void setEmail(String email);

    Set<R> getRoles();

    void setRoles(Set<R> roles);

    void addRole(RoleType roleType);
}
