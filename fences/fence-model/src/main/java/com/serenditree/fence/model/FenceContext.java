package com.serenditree.fence.model;

import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.etc.maple.Maple;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Custom {@link SecurityContext} for the propagation of security information from
 * {@link jakarta.ws.rs.container.ContainerRequestFilter}.
 */
public class FenceContext implements SecurityContext {

    public static final String AUTHENTICATION_SCHEME = "SERENDITREE-JOSE-V1";

    private FencePrincipal principal;

    private boolean secure;

    public FenceContext() {
    }

    public FenceContext(FencePrincipal principal, boolean secure) {
        this.principal = principal;
        this.secure = secure;
    }

    @Override
    public FencePrincipal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return Maple.mapList(this.principal.getRoleTypes(), RoleType::toString).contains(role);
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return AUTHENTICATION_SCHEME;
    }
}
