package com.serenditree.branch.seed.model.entities;

import com.serenditree.fence.model.AbstractTimestampedFenceEntity;
import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.root.data.geo.model.LngLat;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSeed extends AbstractTimestampedFenceEntity<ObjectId> implements FenceEntity<ObjectId> {

    protected ObjectId id;

    protected Integer version;

    protected LngLat location;

    protected String title;

    protected String text;

    protected String username;

    protected Long userId;

    protected Set<String> tags;

    private ObjectId parentId;

    protected List<Nutrition> water;

    protected List<Nutrition> nubits;

    protected boolean anonymous = false;

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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public ObjectId getParentId() {
        return parentId;
    }

    public void setParentId(ObjectId parentId) {
        this.parentId = parentId;
    }

    public List<Nutrition> getWater() {
        return water;
    }

    public void setWater(List<Nutrition> water) {
        this.water = water;
    }

    public List<Nutrition> getNubits() {
        return nubits;
    }

    public void setNubits(List<Nutrition> nubits) {
        this.nubits = nubits;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
