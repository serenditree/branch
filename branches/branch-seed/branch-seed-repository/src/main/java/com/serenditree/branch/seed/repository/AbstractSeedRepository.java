package com.serenditree.branch.seed.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.serenditree.branch.seed.model.entities.AbstractSeed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.repository.api.AbstractSeedRepositoryApi;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import com.serenditree.root.data.nativ.model.Update;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractSeedRepository<E extends AbstractSeed>
        implements AbstractSeedRepositoryApi<E>, PanacheMongoRepository<E> {

    @Inject
    @ConfigProperty(name = "quarkus.mongodb.database")
    String database;

    NativeQueryBuilderApi nativeQueryBuilder;

    MongoClient mongoClient;

    @Override
    public List<E> retrieveByFilter(final SeedFilter filter) {
        NativeQueryBuilderApi nativeQuery = this.nativeQueryBuilder.createNativeQuery();

        if (filter.getBounds() != null) {
            nativeQuery.setBounds(filter.getBounds());
        }

        if (filter.getUserId() != null) {
            nativeQuery.setUserId(filter.getUserId());
        }

        if (filter.getParent() != null) {
            nativeQuery.setParent(filter.getParent(), filter.isTrail());
        }

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            nativeQuery.setTags(filter.getTags());
        }

        if (filter.isPoll()) {
            nativeQuery.setPoll();
        }

        if (filter.isTrail()) {
            nativeQuery.setTrail();
        }

        if (filter.getSort() != null) {
            nativeQuery.setSort(filter.getSort());
        }

        if (filter.getSkip() != null) {
            nativeQuery.setSkip(filter.getSkip());
        }

        if (filter.getLimit() != null) {
            nativeQuery.setLimit(filter.getLimit());
        }

        return StreamSupport
                .stream(
                        this.mongoCollection()
                                .withDocumentClass(this.getEntityType())
                                .aggregate(nativeQuery.build())
                                .spliterator(),
                        false
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<String> retrieveTags(final String name) {
        return StreamSupport
                .stream(
                        this.getCollection()
                                .aggregate(
                                        this.nativeQueryBuilder
                                                .createNativeQuery()
                                                .createTagsQuery(name)
                                                .build()
                                )
                                .spliterator(),
                        false
                )
                .map(document -> document.get("tag").toString())
                .collect(Collectors.toList());
    }

    @Override
    public FenceResponse water(ObjectId id) {
        Update water = this.nativeQueryBuilder.water(id);
        this.getCollection()
                .updateOne(
                        water.getFilter(),
                        water.getDocument()
                );

        return new FenceResponse(id);
    }

    @Override
    public FenceResponse prune(ObjectId id) {
        Update prune = this.nativeQueryBuilder.prune(id);
        this.getCollection()
                .updateOne(
                        prune.getFilter(),
                        prune.getDocument()
                );

        return new FenceResponse(id);
    }

    @Override
    public FenceResponse nubit(ObjectId id, int value) {
        Update nubit = this.nativeQueryBuilder.nubit(id, value);
        this.getCollection()
                .updateOne(
                        nubit.getFilter(),
                        nubit.getDocument()
                );

        return new FenceResponse(id);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVENIENCE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private MongoCollection<Document> getCollection() {
        return this.mongoClient
                .getDatabase(this.database)
                .getCollection(this.getEntityType().getSimpleName());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
}
