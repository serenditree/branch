package io.serenditree.branch.seed.repository;

import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import io.serenditree.root.log.annotation.NotTraced;
import io.serenditree.root.log.annotation.Traced;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch._types.OpenSearchException;

@Dependent
@Traced
@Retry(
    abortOn = {
        OpenSearchException.class
    }
)
public class GardenRepository extends AbstractSeedRepository<Garden> implements GardenRepositoryApi {

    @Override
    @NotTraced
    public Class<Garden> getEntityType() {
        return Garden.class;
    }
}
