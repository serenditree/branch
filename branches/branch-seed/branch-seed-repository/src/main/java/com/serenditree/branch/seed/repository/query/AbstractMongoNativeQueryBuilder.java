package com.serenditree.branch.seed.repository.query;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.*;
import com.serenditree.branch.seed.model.entities.Nutrition;
import com.serenditree.branch.seed.model.filter.SortingType;
import com.serenditree.root.data.geo.model.LngLatBounds;
import com.serenditree.root.data.nativ.api.NativeQueryBuilderApi;
import com.serenditree.root.data.nativ.model.Update;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractMongoNativeQueryBuilder implements NativeQueryBuilderApi {

    private static final Logger LOGGER = Logger.getLogger(AbstractMongoNativeQueryBuilder.class.getName());

    private static final String TEXT_FIELD = "text";
    private static final String TEXT_FIELD_EXPRESSION = "$" + TEXT_FIELD;

    public static final String TOTAL_WATER = "totalWater";
    public static final String WATER_EXPRESSION = "$water";

    public static final String TOTAL_NUBITS = "totalNubits";
    public static final String NUBITS_EXPRESSION = "$nubits";

    public static final String TAG_SCORE = "score";
    public static final String TAG_FIELD = "tag";
    public static final String TAGS_FIELD = "tags";
    public static final String TAGS_FIELD_EXPRESSION = "$" + TAGS_FIELD;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TAG
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final Bson TAG_UNWIND = Aggregates.unwind(TAGS_FIELD_EXPRESSION);
    protected static final Bson TAG_GROUP = Aggregates.group(TAGS_FIELD_EXPRESSION);
    protected static final Bson TAG_SCORE_FILTER = Aggregates.match(Filters.ne(TAG_SCORE, -1));
    protected static final Bson TAG_SCORE_SORT = Aggregates.sort(new Document().append(TAG_SCORE, 1));
    protected static final Bson TAG_PROJECTION = Aggregates.project(Projections.include(TAG_FIELD));

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SORT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final Bson SORT_BY_WATER = Aggregates.sort(Sorts.descending(TOTAL_WATER));
    protected static final Bson SORT_BY_NUBITS = Aggregates.sort(Sorts.descending(TOTAL_NUBITS));
    protected static final Bson SORT_BY_DATE = Aggregates.sort(Sorts.descending("created"));
    protected static final Bson SORT_BY_CHANCE = Aggregates.sample(10);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PAGINATE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final Bson DEFAULT_SKIP = Aggregates.skip(0);
    protected static final Bson DEFAULT_LIMIT = Aggregates.limit(10);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FIELDS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final List<BsonField> INCLUDED_FIELDS = List.of(
        Accumulators.first("created", "$created"),
        Accumulators.first("modified", "$modified"),
        Accumulators.first(TAGS_FIELD, TAGS_FIELD_EXPRESSION),
        Accumulators.first("parent", "$parent"),
        Accumulators.first("location", "$location"),
        Accumulators.first("username", "$username"),
        Accumulators.first("userId", "$userId"),
        Accumulators.first("anonymous", "$anonymous"),
        Accumulators.first("title", "$title"),
        Accumulators.first(TEXT_FIELD, TEXT_FIELD_EXPRESSION)
    );

    protected List<Bson> pipeline;

    protected List<BsonField> includedFields;

    protected List<Bson> filters;

    protected Bson sort;

    protected Bson textLimit;

    protected Bson skip;

    protected Bson limit;

    protected boolean unwind;

    @ConfigProperty(name = "serenditree.seed.water.retention", defaultValue = "21")
    protected int retention;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public NativeQueryBuilderApi createNativeQuery() {
        this.pipeline = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.sort = null;
        this.textLimit = null;
        this.skip = DEFAULT_SKIP;
        this.limit = DEFAULT_LIMIT;
        this.unwind = false;

        return this;
    }

    @Override
    public NativeQueryBuilderApi setBounds(LngLatBounds bounds) {
        this.filters.add(Filters.geoWithinBox(
            "location",
            // southwest and northeast are swapped to account for the persisted order which does not comply
            // to https://docs.mongodb.com/manual/geospatial-queries/#geospatial-legacy
            bounds.getSouth(),
            bounds.getWest(),
            bounds.getNorth(),
            bounds.getEast()
        ));

        return this;
    }

    @Override
    public NativeQueryBuilderApi setUserId(Long userId) {
        this.filters.add(Filters.eq("userId", userId));

        return this;
    }

    @Override
    public NativeQueryBuilderApi setParent(ObjectId parentId, boolean isTrail) {
        Bson filter = Filters.eq("parent", parentId);
        if (isTrail) {
            filter = Filters.or(filter, Filters.eq("_id", parentId));
        }
        this.filters.add(filter);

        return this;
    }

    @Override
    public NativeQueryBuilderApi setTags(Set<String> tags) {
        if (tags.size() == 1) {
            this.filters.add(Filters.eq(TAGS_FIELD, tags.iterator().next()));
        } else {
            throw new WebApplicationException(
                "Only filtering by one tag is supported. You have provided " + tags.size() + ".",
                Response.Status.BAD_REQUEST
            );
        }

        return this;
    }

    @Override
    public NativeQueryBuilderApi setPoll() {
        this.filters.add(Filters.eq("poll", true));

        return this;
    }

    @Override
    public NativeQueryBuilderApi setTrail() {
        this.filters.add(Filters.eq("trail", true));

        return this;
    }

    @Override
    public NativeQueryBuilderApi setSort(SortingType sort) {
        if (sort == SortingType.BY_WATER) {
            this.sort = SORT_BY_WATER;
        } else if (sort == SortingType.BY_NUBITS) {
            this.sort = SORT_BY_NUBITS;
        } else if (sort == SortingType.BY_DATE) {
            this.sort = SORT_BY_DATE;
        } else if (sort == SortingType.BY_CHANCE) {
            this.sort = SORT_BY_CHANCE;
        } else {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return this;
    }

    @Override
    public NativeQueryBuilderApi setRetention(final int retention) {
        this.retention = retention;

        return this;
    }

    @Override
    public NativeQueryBuilderApi setTextLimit(final int maxBytes) {
        List<Bson> fields = this.includedFields.stream()
            .map(BsonField::getName)
            .filter(field -> !field.equals(TEXT_FIELD))
            .map(field -> new Document(field, 1))
            .collect(Collectors.toList());

        fields.add(
            new Document(
                TEXT_FIELD,
                new Document(
                    "$substrBytes",
                    List.of(TEXT_FIELD_EXPRESSION, 0, maxBytes)
                )
            )
        );

        this.textLimit = Aggregates.project(Projections.fields(fields));

        return this;
    }

    @Override
    public NativeQueryBuilderApi setSkip(int skip) {
        this.skip = Aggregates.skip(skip);

        return this;
    }

    @Override
    public NativeQueryBuilderApi setLimit(int limit) {
        this.limit = Aggregates.limit(limit);

        return this;
    }

    @Override
    public NativeQueryBuilderApi createTagsQuery(String name) {
        // TODO this won't scale...
        this.pipeline.add(Aggregates.match(Filters.regex(TAGS_FIELD, ".*" + name + ".*", "i")));
        this.pipeline.add(TAG_UNWIND);
        this.pipeline.add(TAG_GROUP);
        this.pipeline.add(
            Aggregates.project(
                Projections.fields(
                    Projections.excludeId(),
                    Projections.computed("tag", "$_id"),
                    Projections.computed(
                        TAG_SCORE,
                        new Document(
                            "$indexOfCP",
                            List.of(new Document("$toLower", "$_id"), name.toLowerCase())
                        )
                    )
                )
            )
        );
        this.pipeline.add(TAG_SCORE_FILTER);
        this.pipeline.add(TAG_SCORE_SORT);
        this.pipeline.add(TAG_PROJECTION);

        return this;
    }

    @Override
    public Update water(final ObjectId id) {
        return Update.push(
            Filters.eq("_id", id),
            new Document("water", new Nutrition(1))
        );
    }

    @Override
    public Update prune(final ObjectId id) {
        return Update.push(
            Filters.eq("_id", id),
            new Document("water", new Nutrition(-1))
        );
    }

    @Override
    public Update nubit(final ObjectId id, final int value) {
        return Update.push(
            Filters.eq("_id", id),
            new Document("nubit", new Nutrition(value))
        );
    }

    @Override
    public List<Bson> build() {

        if (this.pipeline != null) {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // INITIAL MATCH
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // TODO dynamic time frame/field
            Bson fromFilter = null;
            if (this.sort == SORT_BY_WATER) {
                fromFilter = from("water.added", this.retention);
                this.filters.add(fromFilter);
            } else if (this.sort == SORT_BY_NUBITS) {
                fromFilter = from("nubits.added", this.retention);
                this.filters.add(fromFilter);
            }
            if (!this.filters.isEmpty()) {
                this.pipeline.add(Aggregates.match(Filters.and(this.filters)));
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // NUTRITION AGGREGATES
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (this.sort == SORT_BY_WATER) {
                this.pipeline.add(Aggregates.unwind(WATER_EXPRESSION));
                this.pipeline.add(Aggregates.match(Objects.requireNonNull(fromFilter)));
                this.includedFields.add(Accumulators.sum(TOTAL_WATER, WATER_EXPRESSION + ".value"));
                this.pipeline.add(Aggregates.group("$_id", this.includedFields));
            } else if (this.sort == SORT_BY_NUBITS) {
                this.pipeline.add(Aggregates.unwind(NUBITS_EXPRESSION));
                this.pipeline.add(Aggregates.match(Objects.requireNonNull(fromFilter)));
                this.includedFields.add(Accumulators.sum(TOTAL_NUBITS, NUBITS_EXPRESSION + ".value"));
                this.pipeline.add(Aggregates.group("$_id", this.includedFields));
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // SORT
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (this.sort != null) {
                this.pipeline.add(this.sort);
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // PAGINATE
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (this.sort == null || this.sort != SORT_BY_CHANCE) {
                this.pipeline.add(this.skip);
                this.pipeline.add(this.limit);
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // PROJECTION
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (this.textLimit != null) {
                this.pipeline.add(this.textLimit);
            }
        }

        LOGGER.fine(this::toString);

        return this.pipeline;
    }

    @Override
    public String toJson() {
        return this.toJson(this.pipeline, false);
    }

    @Override
    public String toString() {
        return this.toJson(this.pipeline, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Filters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Bson from(final String field, final int daysAgo) {
        return Filters.gte(
            field,
            LocalDate.now().minus(Period.ofDays(daysAgo))
        );
    }

    private static Bson between(final String field, final LocalDate from, final LocalDateTime to) {
        return Filters.and(
            Filters.gte(field, from),
            Filters.lte(field, to)
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String toJson(List<Bson> documents, boolean nativeQuery) {
        String prefix;
        String suffix;
        if (nativeQuery) {
            prefix = String.join(".", "db", this.getCollection(), "aggregate([");
            suffix = "])";
        } else {
            prefix = "[";
            suffix = "]";
        }

        return documents.stream()
            .map(AbstractMongoNativeQueryBuilder::toJson)
            .collect(Collectors.joining(",", prefix, suffix));
    }

    private static String toJson(Bson bson) {
        return bson.toBsonDocument(
            BsonDocument.class,
            MongoClientSettings.getDefaultCodecRegistry()
        ).toJson();
    }
}
