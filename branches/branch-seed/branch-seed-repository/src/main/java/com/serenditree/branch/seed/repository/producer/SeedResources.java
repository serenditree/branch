package com.serenditree.branch.seed.repository.producer;

import com.serenditree.branch.seed.repository.qualifier.GardenBound;
import com.serenditree.branch.seed.repository.qualifier.SeedBound;
import com.serenditree.branch.seed.repository.query.GardenNativeQueryBuilder;
import com.serenditree.branch.seed.repository.query.SeedNativeQueryBuilder;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class SeedResources {

    /**
     * Getter for seedNativeQueryBuilder.
     *
     * @return value of seedNativeQueryBuilder.
     */
    @Produces
    @RequestScoped
    @SeedBound
    public NativeQueryBuilderApi getSeedNativeQueryBuilder() {
        return new SeedNativeQueryBuilder();
    }

    /**
     * Getter for gardenNativeQueryBuilder.
     *
     * @return value of gardenNativeQueryBuilder.
     */
    @Produces
    @RequestScoped
    @GardenBound
    public NativeQueryBuilderApi getGardenNativeQueryBuilder() {
        return new GardenNativeQueryBuilder();
    }
}
