package com.serenditree.leaf.seed.config;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.service.api.SeedServiceApi;
import com.serenditree.fence.FenceDecorator;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.root.etc.oak.Oak;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import java.util.logging.Logger;

@Decorator
@Priority(Interceptor.Priority.APPLICATION + 100)
@Dependent
public abstract class SeedFence extends FenceDecorator implements SeedServiceApi {

    private static final Logger LOGGER = Logger.getLogger(SeedFence.class.getName());

    protected SeedServiceApi seedService;

    @Override
    public Seed create(Seed seed) {
        if (seed.getParent() != null && seed.isTrail()) {
            this.authorizationService.assertThat(
                    FenceRecordAssertion.fluentBuilder()
                            .setUserId(this.principal.getId().toString())
                            .setEntityId(seed.getParent().toString())
                            .setActionType(FenceActionType.CRUD)
                            .setRecordRequired(true)
                            .build()
            );
        }

        LOGGER.fine(() -> "Applying fence-decoration with principal " + this.principal.getId());
        seed.setText(Oak.html(seed.getText()));
        seed.setUserId(this.principal.getId());
        seed.setUsername(this.principal.getUsername());

        return this.seedService.create(seed);
    }

    @Inject
    public void setSeedService(@Delegate SeedServiceApi seedService) {
        this.seedService = seedService;
    }
}
