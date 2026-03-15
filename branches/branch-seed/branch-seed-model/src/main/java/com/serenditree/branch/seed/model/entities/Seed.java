package com.serenditree.branch.seed.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serenditree.branch.poll.model.entities.Poll;
import jakarta.json.bind.annotation.JsonbProperty;

import java.util.List;


public class Seed extends AbstractSeed {

    public static final String FIELD_POLL = "poll";
    public static final String FIELD_GARDEN_ID = "gardenId";
    public static final String FIELD_TRAIL = "trail";
    public static final String FIELD_TRAIL_ID = "trailId";
    public static final String FIELD_LOCAL_ALIGNMENT = "localAlignment";

    @JsonbProperty(FIELD_POLL)
    private boolean poll = false;

    @JsonIgnore
    private List<Poll> polls;

    @JsonbProperty(FIELD_GARDEN_ID)
    private String gardenId;

    @JsonbProperty(FIELD_TRAIL)
    private boolean trail = false;

    @JsonbProperty(FIELD_TRAIL_ID)
    private String trailId;

    @JsonbProperty(FIELD_LOCAL_ALIGNMENT)
    private int localAlignment;

    @Override
    public void prePersist() {
        super.prePersist();
        this.poll = this.polls != null && !this.polls.isEmpty();
    }

    public boolean isPoll() {
        return poll;
    }

    public void setPoll(boolean poll) {
        this.poll = poll;
    }

    public List<Poll> getPolls() {
        return polls;
    }

    public void setPolls(List<Poll> polls) {
        this.polls = polls;
    }

    public String getGardenId() {
        return gardenId;
    }

    public void setGardenId(String gardenId) {
        this.gardenId = gardenId;
    }

    public boolean isTrail() {
        return trail;
    }

    public void setTrail(boolean trail) {
        this.trail = trail;
    }

    public String getTrailId() {
        return trailId;
    }

    public void setTrailId(String trailId) {
        this.trailId = trailId;
    }

    public int getLocalAlignment() {
        return localAlignment;
    }

    public void setLocalAlignment(int localAlignment) {
        this.localAlignment = localAlignment;
    }
}
