package com.serenditree.branch.seed.repository;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch._types.OpenSearchException;

@Dependent
@Retry(
    abortOn = {
        OpenSearchException.class
    }
)
public class GardenRepository extends AbstractSeedRepository<Garden> implements GardenRepositoryApi {

    @Override
    public Class<Garden> getEntityType() {
        return Garden.class;
    }
}
