package com.serenditree.branch.seed.repository;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.List;

@Dependent
@Retry(
    abortOn = {
        ElasticsearchException.class
    }
)
public class SeedRepository extends AbstractSeedRepository<Seed> implements SeedRepositoryApi {

    @Override
    void setTermFilters(SeedFilter filter, List<Query> filters) {
        super.setTermFilters(filter, filters);

        if (filter.getGardenId() != null) {
            filters.add(this.getTermFilter(Seed.FIELD_GARDEN_ID, filter.getGardenId()));
        }

        if (filter.getTrailId() != null) {
            filters.add(this.getTermFilter(Seed.FIELD_TRAIL_ID, filter.getTrailId()));
        }

        if (filter.isPoll()) {
            filters.add(this.getTermFilter(Seed.FIELD_POLL, true));
        }

        if (filter.isTrail()) {
            filters.add(this.getTermFilter(Seed.FIELD_TRAIL, true));
        }
    }

    @Override
    public Class<Seed> getEntityType() {
        return Seed.class;
    }
}
