package com.serenditree.fence.model;


import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record that holds action based security information.
 */
@Entity
@Table(
        indexes = {
                @Index(name = FenceRecord.USER_REFERENCE, columnList = FenceRecord.USER_REFERENCE),
                @Index(name = FenceRecord.ENTITY_REFERENCE, columnList = FenceRecord.ENTITY_REFERENCE),
                @Index(name = FenceRecord.ACTION_REFERENCE, columnList = FenceRecord.ACTION_REFERENCE),
                @Index(name = FenceRecord.EXPIRATION_REFERENCE, columnList = FenceRecord.EXPIRATION_REFERENCE)
        }
)
@NamedQuery(
        name = FenceRecord.RETRIEVE,
        query = "SELECT r FROM FenceRecord r " +
                "WHERE r.userId = :" + FenceRecord.USER_REFERENCE +
                " AND r.entityId = :" + FenceRecord.ENTITY_REFERENCE +
                " AND r.action = :" + FenceRecord.ACTION_REFERENCE +
                " ORDER BY r.expiration DESC"
)
@NamedQuery(
        name = FenceRecord.RETRIEVE_BY_ENTITY,
        query = "SELECT r FROM FenceRecord r " +
                "WHERE r." + FenceRecord.ENTITY_REFERENCE + " = :" + FenceRecord.ENTITY_REFERENCE
)
@NamedQuery(
        name = FenceRecord.DELETE_BY_ENTITY,
        query = "DELETE FROM FenceRecord " +
                "WHERE " + FenceRecord.ENTITY_REFERENCE + " = :" + FenceRecord.ENTITY_REFERENCE
)
@NamedQuery(
        name = FenceRecord.DELETE_BY_EXPIRATION,
        query = "DELETE FROM FenceRecord " +
                "WHERE " + FenceRecord.EXPIRATION_REFERENCE + " < :" + FenceRecord.EXPIRATION_REFERENCE
)
public class FenceRecord extends AbstractEntity implements FenceEntity<UUID> {

    public static final String USER_REFERENCE = "userId";
    public static final String ENTITY_REFERENCE = "entityId";
    public static final String ACTION_REFERENCE = "action";
    public static final String EXPIRATION_REFERENCE = "expiration";

    public static final String RETRIEVE = "FenceRecord.retrieve";
    public static final String RETRIEVE_BY_ENTITY = "FenceRecord.retrieveByEntity";
    public static final String DELETE_BY_ENTITY = "FenceRecord.deleteByEntity";
    public static final String DELETE_BY_EXPIRATION = "FenceRecord.deleteByExpiration";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)", name = "_id")
    private UUID id;

    @NotNull
    @Column(name = USER_REFERENCE)
    private String userId;

    @NotNull
    @Column(name = ENTITY_REFERENCE)
    private String entityId;

    @NotNull
    @Column(name = ACTION_REFERENCE)
    private String action;

    @Column(name = EXPIRATION_REFERENCE)
    private LocalDateTime expiration;

    public FenceRecord() {
    }

    public FenceRecord(String entityId, String userId, String action, LocalDateTime expiration) {
        this.entityId = entityId;
        this.userId = userId;
        this.action = action;
        this.expiration = expiration;
    }

    public FenceRecord(String entityId, String userId, String action) {
        this.entityId = entityId;
        this.userId = userId;
        this.action = action;
    }

    public FenceRecord(FencePrincipal principal) {
        this.entityId = principal.getId().toString();
        this.userId = principal.getId().toString();
        this.action = FenceActionType.CRUD.name();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }
}
