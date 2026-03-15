package com.serenditree.branch.seed.repository;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import com.serenditree.branch.seed.repository.qualifier.GardenBound;
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
public class GardenRepository
    extends AbstractSeedRepository<Garden>
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
