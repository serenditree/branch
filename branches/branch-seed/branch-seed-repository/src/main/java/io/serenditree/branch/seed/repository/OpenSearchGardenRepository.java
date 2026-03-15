package io.serenditree.branch.seed.repository;

import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.repository.api.GardenRepository;
import io.serenditree.root.log.annotation.NotTraced;
import io.serenditree.root.log.annotation.Traced;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch._types.OpenSearchException;

@Dependent
@Traced
@Retry(abortOn = OpenSearchException.class)
public class OpenSearchGardenRepository extends OpenSearchAbstractSeedRepository<Garden> implements GardenRepository {

    @Override
    @NotTraced
    public Class<Garden> getEntityType() {
        return Garden.class;
    }
}
