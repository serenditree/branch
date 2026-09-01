package io.serenditree.branch.seed.service;

import io.quarkus.logging.Log;
import io.serenditree.branch.seed.exception.RetryConfigServiceException;
import io.serenditree.branch.seed.model.entities.AbstractSeed;
import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.service.api.ConfigService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.analysis.TokenChar;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.IndexSettings;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Class for index creation and configuration.
 */
@Dependent
public class OpenSearchConfigService implements ConfigService {

    private static final String SEED_INDEX = Seed.class.getSimpleName().toLowerCase();
    private static final String GARDEN_INDEX = Garden.class.getSimpleName().toLowerCase();
    private static final List<String> INDICES = List.of(SEED_INDEX, GARDEN_INDEX);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String DATE_FORMAT = "date_hour_minute_second";
    private static final String NGRAM_TOKENIZER = "ngram_tokenizer";
    private static final String NGRAM_ANALYZER = "ngram_analyzer";

    private final OpenSearchClient client;

    @Inject
    public OpenSearchConfigService(OpenSearchClient client) {
        this.client = client;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates the necessary indices with a timestamped name if they do not already exist.
     */
    @Override
    public void createIndices() {
        if (!this.indicesExist()) {
            final String timestamp = LocalDateTime.now().format(FORMATTER);
            INDICES.forEach(name -> this.createIndex(name, timestamp));
        } else {
            Log.info("Indices already exist.");
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new index with a timestamped name, base name alias, custom settings, and type mappings.
     *
     * @param name      The base name of the index to be created.
     * @param timestamp A timestamp string to append to the index name.
     * @throws RetryConfigServiceException if the index creation attempt fails after 4 retries.
     */
    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = 4,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    void createIndex(final String name, final String timestamp) {
        final String timestampedName = "%s-%s".formatted(name, timestamp);
        try {
            boolean created = this.client
                .indices()
                .create(
                    create -> create
                        .index(timestampedName)
                        .aliases(name, alias -> alias.isWriteIndex(true))
                        .settings(this.buildIndexSettings())
                        .mappings(this.buildTypeMapping(name))
                )
                .acknowledged();
            if (created) {
                Log.infov("Created index [{0}].", timestampedName);
            }
        } catch (IOException e) {
            throw new RetryConfigServiceException("Could not create index [" + timestampedName + "].");
        }
    }

    /**
     * Builds index {@link IndexSettings}.
     *
     * @return {@link IndexSettings}
     */
    IndexSettings buildIndexSettings() {

        return new IndexSettings.Builder()
            .maxNgramDiff(3)
            .analysis(
                analysis -> analysis
                    .tokenizer(
                        NGRAM_TOKENIZER,
                        tokenizer -> tokenizer.definition(
                            definition -> definition.ngram(
                                ngram -> ngram
                                    .minGram(3)
                                    .maxGram(6)
                                    .tokenChars(
                                        TokenChar.Letter,
                                        TokenChar.Digit
                                    )
                            )
                        )
                    )
                    .analyzer(
                        "default",
                        analyzer -> analyzer.standard(standard -> standard)
                    )
                    .analyzer(
                        NGRAM_ANALYZER,
                        analyzer -> analyzer.custom(
                            custom -> custom
                                .tokenizer(NGRAM_TOKENIZER)
                                .filter("lowercase")
                        )
                    )
            )
            .build();
    }

    /**
     * Builds {@link TypeMapping}s based on the provided index name.
     *
     * @param name The name of the index for which the type mapping is to be created.
     * @return {@link TypeMapping}
     */
    TypeMapping buildTypeMapping(final String name) {
        // Common mappings
        TypeMapping.Builder typeMappingBuilder = new TypeMapping.Builder()
            .properties(AbstractSeed.FIELD_ID, p -> p.keyword(keyword -> keyword.index(true)))
            .properties(AbstractSeed.FIELD_CREATED, p -> p.date(date -> date.format(DATE_FORMAT).index(true)))
            .properties(AbstractSeed.FIELD_MODIFIED, p -> p.date(date -> date.format(DATE_FORMAT)))
            .properties(
                AbstractSeed.FIELD_LOCATION,
                p -> p.nested(
                    nested -> nested
                        .properties("lat", np -> np.double_(d -> d))
                        .properties("lng", np -> np.double_(d -> d))
                )
            )
            .properties(AbstractSeed.FIELD_GEO_POINT, p -> p.geoPoint(geoPoint -> geoPoint))
            .properties(AbstractSeed.FIELD_PARENT_ID, p -> p.keyword(keyword -> keyword.index(true)))
            .properties(
                AbstractSeed.FIELD_TAGS,
                p -> p.keyword(
                    keyword -> keyword
                        .index(true)
                        .fields(
                            "ngram",
                            fields -> fields.text(
                                text -> text
                                    .index(true)
                                    .analyzer(NGRAM_ANALYZER)
                                    .searchAnalyzer("standard")
                            )
                        )
                )
            )
            .properties(AbstractSeed.FIELD_TITLE, p -> p.text(text -> text.index(true)))
            .properties(AbstractSeed.FIELD_TEXT, p -> p.text(text -> text.index(true)))
            .properties(AbstractSeed.FIELD_USER_ID, p -> p.long_(l -> l.index(true)))
            .properties(AbstractSeed.FIELD_USERNAME, p -> p.keyword(keyword -> keyword.index(true)))
            .properties(AbstractSeed.FIELD_ANONYMOUS, p -> p.boolean_(b -> b.index(true)))
            .properties(
                AbstractSeed.FIELD_WATER,
                p -> p.nested(
                    nested -> nested
                        .properties("added", np -> np.date(date -> date.format(DATE_FORMAT).index(true)))
                        .properties("value", np -> np.short_(s -> s))
                )
            )
            .properties(
                AbstractSeed.FIELD_NUBITS,
                p -> p.nested(
                    nested -> nested
                        .properties("added", np -> np.date(date -> date.format(DATE_FORMAT).index(true)))
                        .properties("value", np -> np.short_(s -> s))
                )
            );

        // Seed-specific mappings
        if (name.equals(SEED_INDEX)) {
            typeMappingBuilder
                .properties(Seed.FIELD_GARDEN_ID, p -> p.keyword(keyword -> keyword.index(true)))
                .properties(Seed.FIELD_LOCAL_ALIGNMENT, p -> p.short_(s -> s.index(true)))
                .properties(Seed.FIELD_POLL, p -> p.boolean_(b -> b.index(true)))
                .properties(Seed.FIELD_TRAIL, p -> p.boolean_(b -> b.index(true)))
                .properties(Seed.FIELD_TRAIL_ID, p -> p.keyword(keyword -> keyword.index(true)));
        }

        return typeMappingBuilder.build();
    }

    /**
     * Determines whether the specified indices exist.
     *
     * @return {@code true} if the indices exist, {@code false} otherwise.
     * @throws RetryConfigServiceException if an I/O error occurs while checking the indices. Retries until successful.
     */
    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = Integer.MAX_VALUE,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    boolean indicesExist() {
        Log.info("Checking indices...");
        try {
            return this.client
                .indices()
                .exists(exists -> exists.index(INDICES))
                .value();
        } catch (IOException e) {
            throw new RetryConfigServiceException("Cluster unavailable.");
        }
    }
}
