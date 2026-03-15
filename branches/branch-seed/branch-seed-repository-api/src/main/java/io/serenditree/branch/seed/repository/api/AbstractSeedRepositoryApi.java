package io.serenditree.branch.seed.repository.api;

import io.serenditree.branch.seed.model.entities.AbstractSeed;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.fence.model.FenceResponse;

import java.io.IOException;
import java.util.List;

public interface AbstractSeedRepositoryApi<E extends AbstractSeed> {

    void persist(E seed) throws IOException;

    E retrieveById(String id) throws IOException;

    List<E> retrieveByFilter(SeedFilter filter) throws IOException;

    List<String> retrieveTags(String name) throws IOException;

    FenceResponse water(String id) throws IOException;

    FenceResponse prune(String id) throws IOException;

    FenceResponse nubit(String id, int value) throws IOException;

    boolean deleteById(String id) throws IOException;

    Long deleteByUser(Long userId) throws IOException;

    Class<E> getEntityType();

    String getIndex();
}
