package com.serenditree.leaf.seed.config;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.service.api.SeedServiceApi;
import com.serenditree.fence.AbstractFenceDecorator;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.root.util.oak.OakHtml;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

@Decorator
@Priority(Interceptor.Priority.APPLICATION + 100)
@Dependent
public abstract class SeedFence extends AbstractFenceDecorator implements SeedServiceApi {

    protected SeedServiceApi seedService;

    @Override
    public Seed create(Seed seed) {
        if (seed.getParentId() != null && seed.isTrail()) {
            this.authorizationService.assertThat(
                FenceRecordAssertion.fluentBuilder()
                    .setUserId(this.principal.getId().toString())
                    .setEntityId(seed.getParentId())
                    .setActionType(FenceActionType.CRUD)
                    .setRecordRequired(true)
                    .build()
            );
        }

        Log.debugv("Applying fence-decoration with principal {0}.", this.principal.getId());
        this.sanitize(seed);
        seed.setUserId(this.principal.getId());
        seed.setUsername(this.principal.getUsername());

        return this.seedService.create(seed);
    }

    /**
     * Checks if there are HTML tags present.
     *
     * @param seed Seed from user input.
     */
    private void sanitize(final Seed seed) {
        OakHtml.sanitize(seed.getTitle());
        OakHtml.sanitize(seed.getText());
        if (seed.getTags() != null) {
            seed.getTags().forEach(OakHtml::sanitize);
        }
        if (seed.getPolls() != null) {
            seed.getPolls().forEach(poll -> {
                OakHtml.sanitize(poll.getTitle());
                poll.getOptions().forEach(option -> OakHtml.sanitize(option.getText()));
            });
        }
    }

    @Inject
    public void setSeedService(@Delegate SeedServiceApi seedService) {
        this.seedService = seedService;
    }
}
