package io.serenditree.branch.user.model.entities;


import io.serenditree.fence.model.api.FenceRole;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.persistence.*;


@Entity
@IdClass(RoleKey.class)
public class Role extends AbstractEntity implements FenceRole<User> {

    @Id
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;

    @Id
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    public Role() {
    }

    public Role(User user, RoleType roleType) {
        this.user = user;
        this.roleType = roleType;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public RoleType getRoleType() {
        return roleType;
    }

    @Override
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }
}
