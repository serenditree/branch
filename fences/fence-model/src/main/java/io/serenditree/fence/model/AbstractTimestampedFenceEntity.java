package io.serenditree.fence.model;

import io.serenditree.fence.model.api.FenceEntity;
import io.serenditree.root.data.generic.model.entities.AbstractTimestampedEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

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
