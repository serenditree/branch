package com.serenditree.fence.model.api;

import com.serenditree.fence.model.enums.RoleType;

import java.security.Principal;
import java.util.List;

public interface FencePrincipal extends Principal {
    Long getId();

    void setId(Long id);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getToken();

    void setToken(String token);

    String getEmail();

    void setEmail(String email);

    List<RoleType> getRoleTypes();

    void setRoleTypes(List<RoleType> roleTypes);

    boolean isInRole(RoleType role);

    void addRole(RoleType role);
}
