package com.serenditree.fence.model;

import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.etc.maple.Maple;

import javax.json.bind.annotation.JsonbTransient;
import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link java.security.Principal} for usage in {@link FenceContext}.
 */
public class Principal implements FencePrincipal {

    private Long id;

    private String username;

    private String password;

    private String token;

    private String email;

    private List<RoleType> roleTypes = new ArrayList<>();

    public Principal() {
    }

    public Principal(Long id, String username, String password, String token, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleType> getRoleTypes() {
        return roleTypes;
    }

    public void setRoleTypes(List<RoleType> roleTypes) {
        this.roleTypes = roleTypes;
    }

    @Override
    @JsonbTransient
    public boolean isInRole(RoleType role) {
        return this.roleTypes
                .stream()
                .anyMatch(r -> r.ordinal() >= role.ordinal());
    }

    @Override
    @JsonbTransient
    public void addRole(RoleType role) {
        this.roleTypes.add(role);
    }

    @Override
    @JsonbTransient
    public String getName() {
        return this.username;
    }

    @Override
    public String toString() {
        return Maple.prettyJson(this);
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
