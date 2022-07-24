package com.serenditree.branch.seed.model.entities;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.seed.model.serializer.ObjectIdDeserializer;
import com.serenditree.branch.seed.model.serializer.ObjectIdSerializer;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.json.bind.annotation.JsonbTypeDeserializer;
import javax.json.bind.annotation.JsonbTypeSerializer;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Cacheable
public class Seed extends AbstractSeed {

    @NotNull
    private int localAlignment;

    @NotNull
    private boolean trail = false;

    @JsonbTypeSerializer(ObjectIdSerializer.class)
    @JsonbTypeDeserializer(ObjectIdDeserializer.class)
    private ObjectId garden;

    @NotNull
    private boolean poll = false;

    @Transient
    @BsonIgnore
    private List<Poll> polls;

    /**
     * Lifecycle hook that sets the poll flag correctly before the entity is persisted for the first time.
     */
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
