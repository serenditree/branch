package com.serenditree.branch.seed.repository;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import com.serenditree.branch.seed.repository.qualifier.GardenBound;
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
public class GardenRepository extends AbstractSeedRepository<Garden>
        implements GardenRepositoryApi, PanacheMongoRepository<Garden> {

    @Override
    @Inject
    public void setNativeQueryBuilder(@GardenBound NativeQueryBuilderApi nativeQueryBuilder) {
        this.nativeQueryBuilder = nativeQueryBuilder;
    }

    @Override
    public Class<Garden> getEntityType() {
        return Garden.class;
    }
}
