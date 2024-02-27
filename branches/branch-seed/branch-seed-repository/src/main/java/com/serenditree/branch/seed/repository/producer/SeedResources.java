package com.serenditree.branch.seed.repository.producer;

import com.serenditree.branch.seed.repository.qualifier.GardenBound;
import com.serenditree.branch.seed.repository.qualifier.SeedBound;
import com.serenditree.branch.seed.repository.query.GardenNativeQueryBuilder;
import com.serenditree.branch.seed.repository.query.SeedNativeQueryBuilder;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SeedResources {

    @ConfigProperty(name = "serenditree.seed.water.retention", defaultValue = "21")
    int retention;

    /**
     * Getter for configured seedNativeQueryBuilder.
     *
     * @return value of seedNativeQueryBuilder.
     */
    @Produces
    @RequestScoped
    @SeedBound
    public NativeQueryBuilderApi getSeedNativeQueryBuilder() {

        return new SeedNativeQueryBuilder()
            .setRetention(this.retention);
    }

    /**
     * Getter for configured gardenNativeQueryBuilder.
     *
     * @return value of gardenNativeQueryBuilder.
     */
    @Produces
    @RequestScoped
    @GardenBound
    public NativeQueryBuilderApi getGardenNativeQueryBuilder() {

        return new GardenNativeQueryBuilder()
            .setRetention(this.retention);
    }
}
