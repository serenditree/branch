package com.serenditree.branch.seed.service.api;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.fence.model.FenceResponse;

import java.util.List;

public interface SeedServiceApi {
    Seed create(Seed seed);

    Seed retrieveById(String id);

    List<Seed> retrieveByFilter(SeedFilter filter);

    List<String> retrieveTags(String name);

    FenceResponse water(String id, String gardenId);

    FenceResponse prune(String id, String gardenId);

    FenceResponse delete(String id);
}
