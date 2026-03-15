package io.serenditree.branch.seed.model.entities;

import io.serenditree.fence.model.AbstractTimestampedFenceEntity;
import io.serenditree.fence.model.api.FenceEntity;
import io.serenditree.root.data.geo.model.LngLat;
import io.serenditree.root.data.geo.model.LonLat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractSeed extends AbstractTimestampedFenceEntity<String> implements FenceEntity<String> {

    public static final String FIELD_ID = "id";
    public static final String FIELD_GEO_POINT = "geoPoint";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_PARENT_ID = "parentId";
    public static final String FIELD_CREATED = "created";

    @JsonbProperty(FIELD_ID)
    protected String id;

    protected LngLat location;

    @JsonbTransient
    protected LonLat geoPoint;

    @JsonbProperty(FIELD_TITLE)
    protected String title;

    @JsonbProperty(FIELD_TEXT)
    protected String text;

    protected String username;

    @JsonbProperty(FIELD_USER_ID)
    protected Long userId;

    @JsonbProperty(FIELD_TAGS)
    protected Set<String> tags;

    @JsonbProperty(FIELD_PARENT_ID)
    protected String parentId;

    @JsonbTransient
    protected List<Nutrition> water;

    @JsonbTransient
    protected List<Nutrition> nubits;

    protected boolean anonymous = false;

    @Override
    public void prePersist() {
        super.prePersist();
        this.id = UUID.randomUUID().toString();
        this.geoPoint = new LonLat(this.location.getLng(), this.location.getLat());
        this.initWater();
        this.initNubit();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    public LonLat getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(LonLat geoPoint) {
        this.geoPoint = geoPoint;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
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

    private void initWater() {
        this.water = new ArrayList<>();
        this.water.add(new Nutrition(1));
    }

    private void initNubit() {
        this.nubits = new ArrayList<>();
        this.nubits.add(new Nutrition(0));
    }
}
