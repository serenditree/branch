package io.serenditree.leaf.seed.config;

import io.quarkus.runtime.Startup;
import io.serenditree.branch.seed.service.api.ConfigService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Startup
@Dependent
public class RootConfig {

    @Inject
    public RootConfig(ConfigService configService) {
        configService.createIndices();
    }
}
