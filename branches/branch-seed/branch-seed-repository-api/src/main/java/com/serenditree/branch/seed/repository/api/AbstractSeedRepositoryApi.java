package com.serenditree.branch.seed.repository.api;

import com.serenditree.branch.seed.model.entities.AbstractSeed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.bson.types.ObjectId;

import java.util.List;

public interface AbstractSeedRepositoryApi<E extends AbstractSeed> extends PanacheMongoRepository<E> {

    List<E> retrieveByFilter(SeedFilter filter);

    List<String> retrieveTags(String name);

    FenceResponse water(ObjectId id);

    FenceResponse prune(ObjectId id);

    FenceResponse nubit(ObjectId id, int value);

    void setNativeQueryBuilder(NativeQueryBuilderApi nativeQueryBuilder);

    Class<E> getEntityType();
}
