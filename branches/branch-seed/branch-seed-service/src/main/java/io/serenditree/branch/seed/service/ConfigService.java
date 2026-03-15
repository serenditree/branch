package io.serenditree.branch.seed.service;

import io.serenditree.branch.seed.exception.RetryConfigServiceException;
import io.serenditree.branch.seed.service.api.ConfigServiceApi;
import io.serenditree.root.util.conifer.Conifer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Dependent
public class ConfigService implements ConfigServiceApi {

    private static final List<String> INDICES = List.of("seed", "garden");

    private final OpenSearchClient client;

    @Inject
    public ConfigService(OpenSearchClient client) {
        this.client = client;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void createIndices() {
        if (!this.indicesExist()) {
            INDICES.forEach(this::createIndex);
        } else {
            Log.info("Indices already exist.");
        }
    }


    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Retry(
        retryOn = RetryConfigServiceException.class,
        maxRetries = 3,
        delay = 4,
        delayUnit = ChronoUnit.SECONDS
    )
    void createIndex(final String name) {
        try {
            Boolean created = this.client
                .indices()
                .create(
                    create -> create
                        .index(name)
                        .settings(settings -> settings.withJson(Conifer.get(name + "-settings.json")))
                        .mappings(mappings -> mappings.withJson(Conifer.get(name + "-mappings.json")))
                )
                .acknowledged();
            if (created != null && created) {
                Log.infov("Created index [{0}].", name);
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
            return this.client
                .indices()
                .exists(exists -> exists.index(INDICES))
                .value();
        } catch (IOException e) {
            throw new RetryConfigServiceException("Cluster unavailable.");
        }
    }
}
