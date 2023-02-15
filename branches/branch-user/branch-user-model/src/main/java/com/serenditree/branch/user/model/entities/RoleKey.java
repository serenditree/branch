package com.serenditree.branch.user.model.entities;

import com.serenditree.fence.model.enums.RoleType;

import javax.annotation.Generated;
import java.io.Serializable;

public class RoleKey implements Serializable {

    private Long user;

    private RoleType roleType;

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    @Override
    @Generated("IDE")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RoleKey roleKey = (RoleKey) o;

        if (!user.equals(roleKey.user)) {
            return false;
        }
        return roleType == roleKey.roleType;
    }

    @Override
    @Generated("IDE")
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + roleType.hashCode();
        return result;
    }
}
