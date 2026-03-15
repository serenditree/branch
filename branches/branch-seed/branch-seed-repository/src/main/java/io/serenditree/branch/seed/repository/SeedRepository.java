package io.serenditree.branch.seed.repository;

import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import io.serenditree.root.log.annotation.NotTraced;
import io.serenditree.root.log.annotation.Traced;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.util.List;

@Dependent
@Traced
@Retry(
    abortOn = {
        OpenSearchException.class
    }
)
public class SeedRepository extends AbstractSeedRepository<Seed> implements SeedRepositoryApi {

    @Override
    protected void setTermFilters(SeedFilter filter, List<Query> filters) {
        super.setTermFilters(filter, filters);

        if (filter.getGardenId() != null) {
            filters.add(this.getTermFilter(Seed.FIELD_GARDEN_ID, FieldValue.of(filter.getGardenId())));
        }

        if (filter.getTrailId() != null) {
            filters.add(this.getTermFilter(Seed.FIELD_TRAIL_ID, FieldValue.of(filter.getTrailId())));
        }

        if (filter.isPoll()) {
            filters.add(this.getTermFilter(Seed.FIELD_POLL, FieldValue.of(true)));
        }

        if (filter.isTrail()) {
            filters.add(this.getTermFilter(Seed.FIELD_TRAIL, FieldValue.of(true)));
        }
    }

    @Override
    @NotTraced
    public Class<Seed> getEntityType() {
        return Seed.class;
    }
}
