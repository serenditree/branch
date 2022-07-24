package com.serenditree.branch.user.model.entities;

import com.serenditree.branch.user.model.serializer.RoleSetSerializer;
import com.serenditree.fence.model.AbstractTimestampedFenceEntity;
import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.fence.model.api.FenceUser;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.data.generic.model.validation.ValidationGroups;
import com.serenditree.root.etc.oak.Oak;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;


@Entity
@Cacheable
@Table(
        indexes = {
                @Index(name = User.USERNAME_REFERENCE, columnList = User.USERNAME_REFERENCE, unique = true),
                @Index(name = User.EMAIL_REFERENCE, columnList = User.EMAIL_REFERENCE, unique = true)
        }
)
@NamedQuery(
        name = User.RETRIEVE_BY_USERNAME,
        query = "SELECT u " +
                "FROM User u " +
                "WHERE u.username = :" + User.USERNAME_REFERENCE
)
@NamedQuery(
        name = User.RETRIEVE_BY_SUBSTRING,
        query = "SELECT u " +
                "FROM User u " +
                "WHERE lower(u.username) LIKE :" + User.SUBSTRING_REFERENCE
)
public class User extends AbstractTimestampedFenceEntity<Long> implements FenceUser<Role>, FenceEntity<Long> {

    public static final String USERNAME_REFERENCE = "username";
    public static final String SUBSTRING_REFERENCE = "sub";
    public static final String EMAIL_REFERENCE = "email";

    public static final String RETRIEVE_BY_USERNAME = "User.retrieveByUsername";
    public static final String RETRIEVE_BY_SUBSTRING = "User.retrieveBySubstring";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PERSISTENT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Id
    @Null(groups = ValidationGroups.Post.class)
    @NotNull(groups = ValidationGroups.Put.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 20)
    @Column(name = USERNAME_REFERENCE)
    private String username;

    @Size(min = 10)
    @Pattern(regexp = Oak.PASSWORD_PATTERN_STRING)
    private String password;

    @Pattern(regexp = Oak.EMAIL_PATTERN_STRING, flags = Pattern.Flag.CASE_INSENSITIVE)
    @Column(name = EMAIL_REFERENCE)
    private String email;


    @Null(groups = ValidationGroups.Post.class)
    @NotNull(groups = ValidationGroups.Put.class)
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonbTypeSerializer(RoleSetSerializer.class)
    private Set<Role> roles;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVENIENCE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addRole(RoleType roleType) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(new Role(this, roleType));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTER/SETTER PERSISTENT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    @JsonbTransient
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @JsonbTransient
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    @Transient
    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
