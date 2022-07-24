package com.serenditree.fence.model;

import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.root.data.generic.model.entities.AbstractTimestampedEntity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.util.List;

@MappedSuperclass
public abstract class AbstractTimestampedFenceEntity<P>
        extends AbstractTimestampedEntity
        implements FenceEntity<P> {
    @JsonbTransient
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = FenceRecord.ENTITY_REFERENCE, insertable = false, updatable = false)
    private List<FenceRecord> fenceRecords;
}
