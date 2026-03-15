package io.serenditree.branch.seed.service.api;

import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.fence.model.FenceResponse;

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

    Long deleteByUser(Long userId);
}
