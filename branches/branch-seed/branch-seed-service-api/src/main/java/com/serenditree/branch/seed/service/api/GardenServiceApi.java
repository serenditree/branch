package com.serenditree.branch.seed.service.api;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.fence.model.FenceResponse;
import org.bson.types.ObjectId;

import java.util.List;

public interface GardenServiceApi {
    Garden create(Garden garden);

    Garden retrieveById(ObjectId id);

    List<Garden> retrieveByFilter(SeedFilter filter);

    List<String> retrieveTags(String name);

    FenceResponse water(ObjectId id);

    FenceResponse prune(ObjectId id);

    FenceResponse delete(ObjectId id);
}
