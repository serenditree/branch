package io.serenditree.leaf.seed.config;

import io.quarkus.logging.Log;
import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.service.api.GardenService;
import io.serenditree.fence.AbstractFenceDecorator;
import io.serenditree.root.util.oak.OakHtml;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

@Decorator
@Priority(Interceptor.Priority.APPLICATION + 100)
@Dependent
public abstract class GardenFence extends AbstractFenceDecorator implements GardenService {

    private final GardenService gardenService;

    @Inject
    protected GardenFence(@Delegate GardenService gardenService) {
        this.gardenService = gardenService;
    }

    @Override
    public Garden create(Garden garden) {
        Log.debugv("Applying fence-decoration with principal {0}.", this.principal.getId());
        OakHtml.sanitize(garden.getTitle());
        OakHtml.sanitize(garden.getText());
        if (garden.getTags() != null) {
            garden.getTags().forEach(OakHtml::sanitize);
        }
        garden.setUserId(this.principal.getId());
        garden.setUsername(this.principal.getUsername());

        return this.gardenService.create(garden);
    }
}
