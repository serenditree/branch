package com.serenditree.branch.seed.repository;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import com.serenditree.branch.seed.repository.qualifier.SeedBound;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import org.eclipse.microprofile.faulttolerance.Retry;

@Dependent
@Retry(
    abortOn = {
        EntityExistsException.class,
        EntityNotFoundException.class,
        NonUniqueResultException.class,
        NoResultException.class
    }
)
public class SeedRepository extends AbstractSeedRepository<Seed> implements
    SeedRepositoryApi,
    PanacheMongoRepository<Seed> {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Inject
    public void setNativeQueryBuilder(@SeedBound NativeQueryBuilderApi nativeQueryBuilder) {
        this.nativeQueryBuilder = nativeQueryBuilder;
    }

    @Override
    public Class<Seed> getEntityType() {
        return Seed.class;
    }
}
