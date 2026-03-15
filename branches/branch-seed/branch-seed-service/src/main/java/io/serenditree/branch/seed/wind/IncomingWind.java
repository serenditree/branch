package io.serenditree.branch.seed.wind;

import io.serenditree.branch.seed.service.api.GardenServiceApi;
import io.serenditree.branch.seed.service.api.SeedServiceApi;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.log.interceptor.LoggedMessageInterceptor;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;

/**
 * Reacts on the creation or deletion of seeds.
 */
@ApplicationScoped
@Logged(binding = LoggedMessageInterceptor.class)
public class IncomingWind {

    private SeedServiceApi seedService;
    private GardenServiceApi gardenService;

    /**
     * Reacts on the deletion of users. Deletes associated seeds.
     *
     * @param message {@link Message} containing the id of the deleted user.
     * @return CompletionStage
     */
    @Transactional
    @Blocking
    @Incoming("user-deleted")
    public CompletionStage<Void> onUserDeleted(final Message<Long> message) {
        final long seedResult = this.seedService.deleteByUser(message.getPayload());
        final long gardenResult = this.gardenService.deleteByUser(message.getPayload());
        Log.debugv("Deleted {0} seed(s) and {1} garden(s) of user {2}", seedResult, gardenResult, message.getPayload());

        return message.ack();
    }

    @Inject
    public void setSeedService(SeedServiceApi seedService) {
        this.seedService = seedService;
    }

    @Inject
    public void setGardenService(GardenServiceApi gardenService) {
        this.gardenService = gardenService;
    }
}
