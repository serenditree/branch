package com.serenditree.branch.seed.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.serenditree.branch.seed.exception.RetryConfigServiceException;
import com.serenditree.branch.seed.service.api.ConfigServiceApi;
import com.serenditree.root.util.conifer.Conifer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Dependent
public class ConfigService implements ConfigServiceApi {

    private static final List<String> INDICES = List.of("seed", "garden");
    private static final String SNAPSHOT_REPOSITORY = "seed-backup";
    private static final String SNAPSHOT_BUCKET = "serenditree-backup";

    private final ElasticsearchClient esClient;

    @Inject
    public ConfigService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void createIndices() {
        if (!this.indicesExist()) {
            INDICES.forEach(this::createIndex);
        } else {
            Log.info("Indices already exist.");
        }
    }

    @Override
    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = 3,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    public void registerSnapshotRepository() {
        Log.info("Checking snapshot repository...");
        try {
            if (esClient.snapshot().getRepository().result().isEmpty()) {
                if (esClient.snapshot().createRepository(
                        create -> create
                            .name(SNAPSHOT_REPOSITORY)
                            .repository(
                                repository -> repository.s3(
                                    s3 -> s3.settings(
                                        settings -> settings
                                            .bucket(SNAPSHOT_BUCKET)
                                            .basePath(SNAPSHOT_REPOSITORY)
                                    )
                                )
                            )
                    )
                    .acknowledged()) {
                    Log.infov("Created snapshot repository [{0}].", SNAPSHOT_REPOSITORY);
                }
            } else {
                Log.info("Snapshot repository already exists.");
            }
        } catch (IOException e) {
            throw new RetryConfigServiceException("Cluster unavailable.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = 3,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    void createIndex(final String name) {
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
            create -> create
                .index(name)
                .withJson(Conifer.get(name + ".json"))
        );

        try {
            if (this.esClient.indices().create(createIndexRequest).acknowledged()) {
                Log.infov("Created index [{0}}].", name);
            }
        } catch (IOException e) {
            throw new RetryConfigServiceException("Could not create index [" + name + "].");
        }
    }

    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = Integer.MAX_VALUE,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    boolean indicesExist() {
        Log.info("Checking indices...");
        try {
            return this.esClient
                .indices()
                .exists(exists -> exists.index(INDICES))
                .value();
        } catch (IOException e) {
            throw new RetryConfigServiceException("Cluster unavailable.");
        }
    }
}
