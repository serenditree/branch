package com.serenditree.leaf.seed.config;

import com.serenditree.branch.seed.service.api.ConfigServiceApi;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Startup
@Dependent
public class RootConfig {

    @Inject
    public RootConfig(ConfigServiceApi configService,
                      @ConfigProperty(name = "serenditree.root.seed.snapshots")
                      boolean snapshotsEnabled) {

        configService.createIndices();

        if (snapshotsEnabled) {
            configService.registerSnapshotRepository();
        } else {
            Log.info("Snapshot repository registration is disabled. Skipping...");
        }
    }
}
