package com.serenditree.branch.seed.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.serenditree.branch.seed.model.entities.AbstractSeed;
import com.serenditree.branch.seed.model.entities.Nutrition;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.model.types.NutritionType;
import com.serenditree.branch.seed.repository.api.AbstractSeedRepositoryApi;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.root.data.geo.model.LngLatBounds;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractSeedRepository<T extends AbstractSeed> implements AbstractSeedRepositoryApi<T> {

    private static final String TAGS_NGRAM = AbstractSeed.FIELD_TAGS + ".ngram";
    private static final String TAGS_UNIQUE_AGGREGATION = "unique_tags";

    private ElasticsearchClient esClient;

    private int retention;

    /**
     * Persists the provided entity.
     * @param seed Entity with ID already set.
     */
    @Override
    public void persist(T seed) throws IOException {
        this.esClient.index(
            index -> index
                .index(this.getIndex())
                .id(seed.getId())
                .document(seed)
        );
    }

    /**
     * Retrieved an entity by ID.
     * @param id ID of the entity to retrieve.
     * @return {@link AbstractSeed} or null.
     */
    @Override
    public T retrieveById(String id) throws IOException {
        GetResponse<T> response = this.esClient.get(
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
        Query query = QueryBuilders.bool(bool -> bool.filter(filters));

        // Set sort
        List<SortOptions> sortOptions = new ArrayList<>();
        if (filter.getSort() != null) {
            switch (filter.getSort()) {
                case BY_WATER -> sortOptions.add(this.getSortByNutrition(NutritionType.WATER));
                case BY_NUBITS -> sortOptions.add(this.getSortByNutrition(NutritionType.NUBITS));
                case BY_DATE -> sortOptions.add(this.getSortByDate());
                case BY_CHANCE -> query = this.getSortByChance(filters); // Replaces bool query by function score!!
            }
        }

        Query finalQuery = query;
        SearchRequest searchRequest = SearchRequest.of(
            search -> search
                .index(this.getIndex())
                .query(finalQuery)
                .sort(sortOptions)
                .from(filter.getSkip())
                .size(filter.getLimit())
        );

        Log.debug(searchRequest);

        return esClient.search(searchRequest, this.getEntityType())
            .hits().hits()
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
                    QueryBuilders.match(
                        match -> match
                            .field(TAGS_NGRAM)
                            .query(ngram)
                    )
                )
                .aggregations(
                    TAGS_UNIQUE_AGGREGATION,
                    agg -> agg.terms(
                        terms -> terms
                            .field(AbstractSeed.FIELD_TAGS)
                            .size(10)
                    )
                )
                .size(0)
        );

        Log.debug(searchRequest);

        return esClient.search(searchRequest, Void.class)
            .aggregations()
            .get(TAGS_UNIQUE_AGGREGATION)
            .sterms().buckets().array()
            .stream()
            .map(bucket -> bucket.key().stringValue())
            .filter(tag -> tag.contains(ngram))
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
        this.esClient.delete(
            delete -> delete
                .index(this.getIndex())
                .id(id)
        );

        return true;
    }

    @Override
    public String getIndex() {
        return this.getEntityType()
            .getSimpleName()
            .toLowerCase();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Query getGeoBoundingBoxQuery(final LngLatBounds bounds) {
        return QueryBuilders.geoBoundingBox(
            geoBoundingBox -> geoBoundingBox
                .field(AbstractSeed.FIELD_GEO_POINT)
                .boundingBox(
                    boundingBox -> boundingBox
                        .coords(cords -> cords
                            .top(bounds.getNorth())
                            .right(bounds.getEast())
                            .bottom(bounds.getSouth())
                            .left(bounds.getWest()))
                )
        );
    }

    Query getNestedRangeQuery(NutritionType type) {
        Query rangeQuery = QueryBuilders.range(
            range -> range
                .date(
                    dateRange -> dateRange
                        .field(type + "." + Nutrition.FIELD_ADDED)
                        .gte(String.valueOf(LocalDate.now().minusDays(this.retention)))
                )
        );


        return QueryBuilders.nested(
            nested -> nested
                .path(type.toString())
                .query(QueryBuilders.bool(bool -> bool.filter(rangeQuery)))
        );
    }

    SortOptions getSortByNutrition(NutritionType type) {
        NestedSortValue nestedSort = new NestedSortValue.Builder()
            .path(type.toString())
            .filter(this.getNestedRangeQuery(type))
            .build();

        return SortOptionsBuilders.field(
            sort -> sort
                .field(type + "." + Nutrition.FIELD_VALUE)
                .order(SortOrder.Desc)
                .mode(SortMode.Sum)
                .nested(nestedSort)
        );
    }

    SortOptions getSortByDate() {
        return SortOptionsBuilders.field(
            sort -> sort
                .field(AbstractSeed.FIELD_CREATED)
                .order(SortOrder.Desc)
        );
    }

    Query getSortByChance(final List<Query> filters) {
        return QueryBuilders.functionScore(
            functionScore -> functionScore
                .query(QueryBuilders.bool(bool -> bool.filter(filters)))
                .boostMode(FunctionBoostMode.Replace)
                .functions(
                    function -> function
                        .randomScore(
                            RandomScoreFunction.of(
                                rand -> rand
                            )
                        )
                )
        );
    }

    Query getTermFilter(final String field, final String value) {
        return QueryBuilders.term(
            term -> term
                .field(field)
                .value(value)
        );
    }

    Query getTermFilter(final String field, final long value) {
        return QueryBuilders.term(
            term -> term
                .field(field)
                .value(value)
        );
    }

    Query getTermFilter(final String field, final boolean value) {
        return QueryBuilders.term(
            term -> term
                .field(field)
                .value(value)
        );
    }

    void setTermFilters(final SeedFilter filter, final List<Query> filters) {
        if (filter.getUserId() != null) {
            filters.add(this.getTermFilter(AbstractSeed.FIELD_USER_ID, filter.getUserId()));
        }

        if (filter.getParentId() != null) {
            filters.add(this.getTermFilter(AbstractSeed.FIELD_PARENT_ID, filter.getParentId()));
        }

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            if (filter.getTags().size() == 1) {
                filters.add(this.getTermFilter(AbstractSeed.FIELD_TAGS, filter.getTags().iterator().next()));
            } else {
                throw new WebApplicationException("Filter by one tag only!", Response.Status.BAD_REQUEST);
            }
        }
    }

    void addNutrition(final NutritionType type, final String id, final int value) throws IOException {
        this.esClient.update(
            update -> update
                .index(this.getIndex())
                .id(id)
                .script(
                    script -> script
                        .source("ctx._source." + type + ".add(params.nutrition)")
                        .params("nutrition", JsonData.of(new Nutrition(value)))
                ),
            Void.class
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setEsClient(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @ConfigProperty(name = "serenditree.seed.water.retention", defaultValue = "21")
    public void setRetention(int retention) {
        this.retention = retention;
    }
}
