package com.serenditree.branch.seed.model.entities;

import com.serenditree.branch.poll.model.entities.Poll;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.List;


public class Seed extends AbstractSeed {

    private boolean poll = false;

    @BsonIgnore
    private List<Poll> polls;

    private ObjectId gardenId;

    private boolean trail = false;

    private ObjectId trailId;

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

    public ObjectId getGardenId() {
        return gardenId;
    }

    public void setGardenId(ObjectId gardenId) {
        this.gardenId = gardenId;
    }

    public boolean isTrail() {
        return trail;
    }

    public void setTrail(boolean trail) {
        this.trail = trail;
    }

    public ObjectId getTrailId() {
        return trailId;
    }

    public void setTrailId(ObjectId trailId) {
        this.trailId = trailId;
    }

    public int getLocalAlignment() {
        return localAlignment;
    }

    public void setLocalAlignment(int localAlignment) {
        this.localAlignment = localAlignment;
    }
}
