package com.serenditree.fence.model;


import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import com.serenditree.root.util.oak.OakDate;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Record that holds action based security information.
 */
@Entity
@Cacheable
@Table(
    indexes = {
        @Index(name = "idx_fence_record_entity_id", columnList = FenceRecord.ENTITY_REFERENCE),
        @Index(name = "idx_fence_record_expiration", columnList = FenceRecord.EXPIRATION_REFERENCE),
    }
)
@IdClass(FenceRecord.class)
@NamedQuery(
    name = FenceRecord.RETRIEVE,
    query = "SELECT r FROM FenceRecord r " +
            "WHERE r.userId = :" + FenceRecord.USER_REFERENCE +
            " AND r.entityId = :" + FenceRecord.ENTITY_REFERENCE +
            " AND r.action = :" + FenceRecord.ACTION_REFERENCE +
            " ORDER BY r.expiration DESC",
    hints = @QueryHint(name = "org.hibernate.cacheable", value = "true")
)
@NamedQuery(
    name = FenceRecord.RETRIEVE_BY_ENTITY,
    query = "SELECT r FROM FenceRecord r " +
            "WHERE r." + FenceRecord.ENTITY_REFERENCE + " = :" + FenceRecord.ENTITY_REFERENCE,
    hints = @QueryHint(name = "org.hibernate.cacheable", value = "true")
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
public class FenceRecord extends AbstractEntity {

    public static final String USER_REFERENCE = "userId";
    public static final String ENTITY_REFERENCE = "entityId";
    public static final String ACTION_REFERENCE = "action";
    public static final String EXPIRATION_REFERENCE = "expiration";

    public static final String RETRIEVE = "FenceRecord.retrieve";
    public static final String RETRIEVE_BY_ENTITY = "FenceRecord.retrieveByEntity";
    public static final String DELETE_BY_ENTITY = "FenceRecord.deleteByEntity";
    public static final String DELETE_BY_EXPIRATION = "FenceRecord.deleteByExpiration";

    @Id
    @Column(name = USER_REFERENCE)
    private Long userId;

    @Id
    @Column(name = ENTITY_REFERENCE)
    private String entityId;

    @Id
    @Column(name = ACTION_REFERENCE)
    private String action;

    @Id
    @Column(name = EXPIRATION_REFERENCE)
    private LocalDateTime expiration = OakDate.POSITIVE_INFINITY;

    public FenceRecord() {
    }

    public FenceRecord(String entityId, Long userId, String action, LocalDateTime expiration) {
        this.entityId = entityId;
        this.userId = userId;
        this.action = action;
        this.expiration = expiration;
    }

    public FenceRecord(String entityId, Long userId, String action) {
        this.entityId = entityId;
        this.userId = userId;
        this.action = action;
    }

    public FenceRecord(FencePrincipal principal) {
        this.entityId = principal.getId().toString();
        this.userId = principal.getId();
        this.action = FenceActionType.CRUD.name();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
