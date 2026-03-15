package com.serenditree.fence.model;

import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

import java.util.List;

@MappedSuperclass
public abstract class AbstractFenceEntity<P>
    extends AbstractEntity
    implements FenceEntity<P> {
    @JsonbTransient
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = FenceRecord.ENTITY_REFERENCE, insertable = false, updatable = false)
    private List<FenceRecord> fenceRecords;
}
