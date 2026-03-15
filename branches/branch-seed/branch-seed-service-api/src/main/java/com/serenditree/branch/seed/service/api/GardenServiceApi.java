package com.serenditree.branch.seed.service.api;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.fence.model.FenceResponse;

import java.io.IOException;
import java.util.List;

public interface GardenServiceApi {
    Garden create(Garden garden);

    Garden retrieveById(String id);

    List<Garden> retrieveByFilter(SeedFilter filter);

    List<String> retrieveTags(String name);

    FenceResponse water(String id) throws IOException;

    FenceResponse prune(String id) throws IOException;

    FenceResponse delete(String id);
}
