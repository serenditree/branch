package com.serenditree.branch.seed.service.api;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.fence.model.FenceResponse;
import org.bson.types.ObjectId;

import java.util.List;

public interface SeedServiceApi {
    Seed create(Seed seed);

    Seed retrieveById(ObjectId id);

    List<Seed> retrieveByFilter(SeedFilter filter);

    List<String> retrieveTags(String name);

    FenceResponse water(ObjectId id, ObjectId gardenId);

    FenceResponse prune(ObjectId id, ObjectId gardenId);

    FenceResponse delete(ObjectId id);
}
