package com.serenditree.branch.seed.wind.api;

import com.serenditree.branch.seed.model.entities.Seed;
import org.bson.types.ObjectId;

public interface OutgoingWindApi {
    void releaseSeedCreated(Seed id);

    void releaseSeedDeleted(ObjectId id);
}
