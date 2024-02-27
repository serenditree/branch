package com.serenditree.branch.seed.model.entities;

import com.serenditree.branch.poll.model.entities.Poll;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.List;


public class Seed extends AbstractSeed {

    private int localAlignment;

    private boolean trail = false;

    private ObjectId garden;

    private boolean poll = false;

    @BsonIgnore
    private List<Poll> polls;

    @Override
    public void prePersist() {
        super.prePersist();
        this.poll = this.polls != null && !this.polls.isEmpty();
    }

    public int getLocalAlignment() {
        return localAlignment;
    }

    public void setLocalAlignment(int localAlignment) {
        this.localAlignment = localAlignment;
    }

    public boolean isTrail() {
        return trail;
    }

    public void setTrail(boolean trail) {
        this.trail = trail;
    }

    public ObjectId getGarden() {
        return garden;
    }

    public void setGarden(ObjectId garden) {
        this.garden = garden;
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
}
