package io.serenditree.leaf.seed.config;

import io.serenditree.branch.seed.service.api.ConfigServiceApi;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Startup
@Dependent
public class RootConfig {

    @Inject
    public RootConfig(ConfigServiceApi configService) {
        configService.createIndices();
    }
}
