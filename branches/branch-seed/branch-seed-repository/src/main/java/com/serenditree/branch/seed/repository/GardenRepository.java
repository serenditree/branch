package com.serenditree.branch.seed.repository;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;

@Dependent
@Retry(
    abortOn = {
        ElasticsearchException.class
    }
)
public class GardenRepository extends AbstractSeedRepository<Garden> implements GardenRepositoryApi {

    @Override
    public Class<Garden> getEntityType() {
        return Garden.class;
    }
}
