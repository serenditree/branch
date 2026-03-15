package com.serenditree.branch.seed.repository.query;

import com.mongodb.client.model.Accumulators;
import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;

import java.util.ArrayList;

public class SeedNativeQueryBuilder extends AbstractMongoNativeQueryBuilder implements NativeQueryBuilderApi {

    public SeedNativeQueryBuilder() {
        this.includedFields = new ArrayList<>(AbstractMongoNativeQueryBuilder.INCLUDED_FIELDS);
        this.includedFields.add(Accumulators.first("poll", "$poll"));
        this.includedFields.add(Accumulators.first("gardenId", "$gardenId"));
        this.includedFields.add(Accumulators.first("trail", "$trail"));
        this.includedFields.add(Accumulators.first("trailId", "$trailId"));
        this.includedFields.add(Accumulators.first("localAlignment", "$localAlignment"));
    }

    @Override
    public String getCollection() {
        return Seed.class.getSimpleName();
    }
}
