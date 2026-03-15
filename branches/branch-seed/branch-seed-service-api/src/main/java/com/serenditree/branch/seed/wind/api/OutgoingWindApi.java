package com.serenditree.branch.seed.wind.api;

import com.serenditree.branch.seed.model.entities.Seed;

public interface OutgoingWindApi {
    void releaseSeedCreated(Seed id);

    void releaseSeedDeleted(String id);
}
