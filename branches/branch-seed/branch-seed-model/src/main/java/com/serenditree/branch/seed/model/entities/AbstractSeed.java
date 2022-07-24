package com.serenditree.branch.seed.model.entities;

import com.serenditree.branch.seed.model.serializer.ObjectIdDeserializer;
import com.serenditree.branch.seed.model.serializer.ObjectIdSerializer;
import com.serenditree.fence.model.AbstractTimestampedFenceEntity;
import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.root.data.generic.model.validation.ValidationGroups;
import com.serenditree.root.data.geo.model.LngLat;
import org.bson.types.ObjectId;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeDeserializer;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@MappedSuperclass
public abstract class AbstractSeed extends AbstractTimestampedFenceEntity<ObjectId> implements FenceEntity<ObjectId> {

    @Id
    @Null(groups = ValidationGroups.Post.class)
    @NotNull(groups = ValidationGroups.Put.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonbTypeSerializer(ObjectIdSerializer.class)
    @JsonbTypeDeserializer(ObjectIdDeserializer.class)
    protected ObjectId id;

    @Version
    protected Integer version;

    @NotNull
    @Embedded
    protected LngLat location;

    @NotBlank
    protected String title;

    @NotBlank
    @Lob
    protected String text;

    @NotBlank
    protected String username;

    @NotNull
    protected Long userId;

    @NotNull
    protected boolean anonymous = false;

    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<String> tags;

    @JsonbTypeSerializer(ObjectIdSerializer.class)
    @JsonbTypeDeserializer(ObjectIdDeserializer.class)
    private ObjectId parent;

    @JsonbTransient
    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn(name = "order", nullable = false, updatable = false)
    protected List<Nutrition> water;

    @JsonbTransient
    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn(name = "order", nullable = false, updatable = false)
    protected List<Nutrition> nubits;

    public void water() {
        this.water.add(new Nutrition(1));
        this.setModified();
    }

    public void prune() {
        this.water.add(new Nutrition(-1));
        this.setModified();
    }

    public void nubit(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("nubit must be greater than zero");
        }
        this.nubits.add(new Nutrition(value));
        this.setModified();
    }

    /**
     * Lifecycle hook that guarantees that {@link Nutrition} containers are initialized before the entity
     * is persisted for the first time.
     */
    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        this.water = new ArrayList<>();
        this.water();
        this.nubits = new ArrayList<>();
        this.nubits.add(new Nutrition(0));
    }

    @Override
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public ObjectId getParent() {
        return parent;
    }

    public void setParent(ObjectId parent) {
        this.parent = parent;
    }

    public List<Nutrition> getWater() {
        return water;
    }

    private void setWater(List<Nutrition> water) {
        this.water = water;
    }

    public List<Nutrition> getNubits() {
        return nubits;
    }

    private void setNubits(List<Nutrition> nubits) {
        this.nubits = nubits;
    }
}
