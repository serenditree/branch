package io.serenditree.branch.seed.repository;

import io.serenditree.branch.seed.model.entities.AbstractSeed;
import io.serenditree.branch.seed.model.entities.Nutrition;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.branch.seed.model.types.NutritionType;
import io.serenditree.branch.seed.repository.api.AbstractSeedRepositoryApi;
import io.serenditree.fence.model.FenceResponse;
import io.serenditree.root.data.geo.model.LngLatBounds;
import io.serenditree.root.log.annotation.NotTraced;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.*;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractSeedRepository<T extends AbstractSeed> implements AbstractSeedRepositoryApi<T> {

    private static final int RETENTION_DAYS = ConfigProvider
        .getConfig()
        .getValue("serenditree.seed.retention", Integer.class);
    private static final String RETENTION = "now-%dd".formatted(RETENTION_DAYS);
    private static final String TAGS_NGRAM = "%s.ngram".formatted(AbstractSeed.FIELD_TAGS);
    private static final String TAGS_UNIQUE_AGGREGATION = "unique_tags";

    private OpenSearchClient client;

    /**
     * Persists the provided entity.
     * @param seed Entity with ID already set.
     */
    @Override
    public void persist(T seed) throws IOException {
        this.client.index(
            index -> index
                .index(this.getIndex())
                .id(seed.getId())
                .document(seed)
        );
    }

    /**
     * Retrieves an entity by ID.
     * @param id ID of the entity to retrieve.
     * @return {@link AbstractSeed} or null.
     */
    @Override
    public T retrieveById(String id) throws IOException {
        GetResponse<T> response = this.client.get(
            get -> get
                .index(this.getIndex())
                .id(id),
            this.getEntityType()
        );

        T seed = null;
        if (response.found()) {
            seed = response.source();
        }

        return seed;
    }

    /**
     * Retrieves entities by the evaluation of a filter of type {@link SeedFilter}. Filter and sorting information is
     * translated into a search request.
     * @param filter Filter definition.
     * @return Filtered list of entities.
     */
    @Override
    public List<T> retrieveByFilter(final SeedFilter filter) throws IOException {
        // Set filters
        List<Query> filters = new ArrayList<>();
        if (filter.getBounds() != null) {
            filters.add(this.getGeoBoundingBoxQuery(filter.getBounds()));
        }

        this.setTermFilters(filter, filters);
        Query query = QueryBuilders
            .bool()
            .filter(filters)
            .build()
            .toQuery();

        // Set sort
        List<SortOptions> sortOptions = new ArrayList<>();
        if (filter.getSort() != null) {
            switch (filter.getSort()) {
                case BY_WATER -> sortOptions.add(this.getSortByNutrition(NutritionType.WATER));
                case BY_NUBITS -> sortOptions.add(this.getSortByNutrition(NutritionType.NUBITS));
                case BY_DATE -> sortOptions.add(this.getSortByDate());
                case BY_CHANCE -> query = this.getSortByChance(filters); // Replaces a bool query by function score!!
            }
        }

        final Query finalQuery = query;
        SearchRequest searchRequest = SearchRequest.of(
            search -> search
                .index(this.getIndex())
                .query(finalQuery)
                .sort(sortOptions)
                .from(filter.getSkip())
                .size(filter.getLimit())
        );

        Log.debug(searchRequest.toJsonString());

        return client.search(searchRequest, this.getEntityType())
            .hits()
            .hits()
            .stream()
            .map(Hit::source)
            .toList();
    }

    /**
     * Retrieves the top ten existing tags by partial matching sorted by document count.
     * @param ngram Partial name of a tag.
     * @return List of unique tags.
     */
    @Override
    public List<String> retrieveTags(final String ngram) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(
            search -> search
                .index(this.getIndex())
                .query(
                    query -> query.match(
                        match -> match
                            .field(TAGS_NGRAM)
                            .query(FieldValue.of(ngram))
                    )
                )
                .aggregations(
                    TAGS_UNIQUE_AGGREGATION,
                    agg -> agg.terms(
                        terms -> terms
                            .field(AbstractSeed.FIELD_TAGS)
                            .include(include -> include.regexp(".*" + ngram + ".*"))
                            .size(10)
                    )
                )
                .size(0)
        );

        Log.debug(searchRequest.toJsonString());

        return client.search(searchRequest, Void.class)
            .aggregations()
            .get(TAGS_UNIQUE_AGGREGATION)
            .sterms()
            .buckets()
            .array()
            .stream()
            .map(StringTermsBucket::key)
            .toList();
    }

    @Override
    public FenceResponse water(final String id) throws IOException {
        this.addNutrition(NutritionType.WATER, id, 1);

        return new FenceResponse(id);
    }

    @Override
    public FenceResponse prune(final String id) throws IOException {
        this.addNutrition(NutritionType.WATER, id, -1);

        return new FenceResponse(id);
    }

    @Override
    public FenceResponse nubit(final String id, final int value) throws IOException {
        this.addNutrition(NutritionType.NUBITS, id, value);

        return new FenceResponse(id);
    }

    @Override
    public boolean deleteById(final String id) throws IOException {
        DeleteResponse response = this.client.delete(
            delete -> delete
                .index(this.getIndex())
                .id(id)
        );

        return response.result() == Result.Deleted;
    }

    @Override
    public Long deleteByUser(final Long userId) throws IOException {
        DeleteByQueryResponse response = this.client.deleteByQuery(
            delete -> delete
                .index(this.getIndex())
                .query(
                    query -> query.term(
                        term -> term
                            .field(AbstractSeed.FIELD_USER_ID)
                            .value(FieldValue.of(userId))
                    )
                )
        );

        return response.deleted();
    }

    @Override
    @NotTraced
    public String getIndex() {
        return this.getEntityType()
            .getSimpleName()
            .toLowerCase();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Query getGeoBoundingBoxQuery(final LngLatBounds bounds) {
        return QueryBuilders.geoBoundingBox()
            .field(AbstractSeed.FIELD_GEO_POINT)
            .boundingBox(
                geoBoundingBox -> geoBoundingBox.coords(
                    coords -> coords
                        .top(bounds.getNorth())
                        .right(bounds.getEast())
                        .bottom(bounds.getSouth())
                        .left(bounds.getWest())
                )
            )
            .build()
            .toQuery();
    }

    private SortOptions getSortByNutrition(final NutritionType type) {
        NestedSortValue nestedSort = new NestedSortValue.Builder()
            .path(type.toString())
            .filter(
                QueryBuilders.range()
                    .field(type + "." + Nutrition.FIELD_ADDED)
                    .gte(JsonData.of(RETENTION))
                    .build()
                    .toQuery())
            .build();

        return SortOptionsBuilders
            .field()
            .nested(nestedSort)
            .field(type + "." + Nutrition.FIELD_VALUE)
            .mode(SortMode.Sum)
            .order(SortOrder.Desc)
            .build()
            ._toSortOptions();
    }

    private SortOptions getSortByDate() {
        return SortOptionsBuilders
            .field()
            .field(AbstractSeed.FIELD_CREATED)
            .order(SortOrder.Desc)
            .build()
            ._toSortOptions();
    }

    private Query getSortByChance(final List<Query> filters) {
        return QueryBuilders
            .functionScore()
            .query(
                QueryBuilders
                    .bool()
                    .filter(filters)
                    .build()
                    .toQuery()
            )
            .boostMode(FunctionBoostMode.Replace)
            .functions(
                new FunctionScore
                    .Builder()
                    .randomScore(
                        new RandomScoreFunction
                            .Builder()
                            .build()
                    )
                    .build()
            )
            .build()
            .toQuery();
    }

    protected Query getTermFilter(final String field, final FieldValue value) {
        return QueryBuilders
            .term()
            .field(field)
            .value(value)
            .build()
            .toQuery();
    }

    protected void setTermFilters(final SeedFilter filter, final List<Query> filters) {
        if (filter.getUserId() != null) {
            filters.add(this.getTermFilter(AbstractSeed.FIELD_USER_ID, FieldValue.of(filter.getUserId())));
        }

        if (filter.getParentId() != null) {
            filters.add(this.getTermFilter(AbstractSeed.FIELD_PARENT_ID, FieldValue.of(filter.getParentId())));
        }

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            if (filter.getTags().size() == 1) {
                filters.add(
                    this.getTermFilter(AbstractSeed.FIELD_TAGS, FieldValue.of(filter.getTags().iterator().next()))
                );
            } else {
                throw new WebApplicationException("Filter by one tag only!", Response.Status.BAD_REQUEST);
            }
        }
    }

    protected void addNutrition(final NutritionType type, final String id, final int value) throws IOException {
        this.client.update(
            update -> update
                .index(this.getIndex())
                .id(id)
                .script(
                    script -> script.inline(
                        function -> function
                            .source("ctx._source." + type + ".add(params.nutrition)")
                            .params("nutrition", JsonData.of(new Nutrition(value)))
                    )
                ),
            Void.class
        );
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setClient(OpenSearchClient client) {
        this.client = client;
    }
}
