package io.serenditree.fence.model.api;

import io.serenditree.fence.model.enums.RoleType;

public interface FenceRole<U extends FenceUser> {
    RoleType getRoleType();

    void setRoleType(RoleType roleType);

    U getUser();

    void setUser(U user);
}
