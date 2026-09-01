package io.serenditree.branch.seed.wind.api;

import io.serenditree.branch.seed.model.entities.Seed;

public interface OutgoingWind {
    void releaseSeedCreated(Seed id);

    void releaseSeedDeleted(String id);
}
