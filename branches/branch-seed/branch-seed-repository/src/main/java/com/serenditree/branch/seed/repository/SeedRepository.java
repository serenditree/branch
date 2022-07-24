package com.serenditree.branch.seed.repository;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import com.serenditree.branch.seed.repository.qualifier.SeedBound;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.eclipse.microprofile.faulttolerance.Retry;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

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
