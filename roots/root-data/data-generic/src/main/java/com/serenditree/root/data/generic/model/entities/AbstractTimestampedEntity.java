package com.serenditree.root.data.generic.model.entities;


import com.serenditree.root.util.oak.OakDate;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * Superclass of all entities where creation- and modification-timestamps should be created and/or updated automatically
 * using lifecycle-hooks.
 */
@MappedSuperclass
public abstract class AbstractTimestampedEntity extends AbstractEntity {

    private LocalDateTime created;

    private LocalDateTime modified;

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    /**
     * Sets current {@link LocalDateTime} of created and modified when the entity is persisted for the first time.
     */
    protected void setCreated() {
        LocalDateTime now = OakDate.now();
        this.created = now;
        this.modified = now;
    }

    /**
     * Sets current {@link LocalDateTime} of created and modified each time the entity is updated.
     */
    protected void setModified() {
        this.modified = OakDate.now();
    }

    /**
     * Sets current {@link LocalDateTime} of created and modified when the entity is persisted for the first time.
     */
    @PrePersist
    protected void prePersist() {
        this.setCreated();
    }

    /**
     * Sets current {@link LocalDateTime} of created and modified each time the entity is updated.
     */
    @PreUpdate
    protected void preUpdate() {
        this.setModified();
    }
}
