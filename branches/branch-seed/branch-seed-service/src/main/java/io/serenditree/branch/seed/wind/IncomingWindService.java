package io.serenditree.branch.seed.wind;

import io.quarkus.logging.Log;
import io.serenditree.branch.seed.service.api.GardenService;
import io.serenditree.branch.seed.service.api.SeedService;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.log.interceptor.LoggedMessageInterceptor;
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
public class IncomingWindService {

    private final SeedService seedService;
    private final GardenService gardenService;

    @Inject
    public IncomingWindService(SeedService seedService, GardenService gardenService) {
        this.seedService = seedService;
        this.gardenService = gardenService;
    }

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
}
