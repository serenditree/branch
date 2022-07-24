package com.serenditree.branch.seed.repository.query;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;

import java.util.ArrayList;

public class GardenNativeQueryBuilder extends AbstractMongoNativeQueryBuilder implements NativeQueryBuilderApi {

    public GardenNativeQueryBuilder() {
        this.includedFields = new ArrayList<>(AbstractMongoNativeQueryBuilder.INCLUDED_FIELDS);
    }

    @Override
    public String getCollection() {
        return Garden.class.getSimpleName();
    }
}
