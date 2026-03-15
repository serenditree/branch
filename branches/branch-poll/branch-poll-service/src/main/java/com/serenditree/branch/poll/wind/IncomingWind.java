package com.serenditree.branch.poll.wind;

import com.serenditree.branch.poll.service.PollService;
import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.log.interceptor.LoggedMessageInterceptor;
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

    private PollService pollService;

    /**
     * Reacts on the creation of seeds.
     * TODO move to a monitoring consumer (poc-only).
     *
     * @param message {@link Message} containing information of the created seed.
     * @return CompletionStage
     */
    @Incoming("seed-created")
    public CompletionStage<Void> onSeedCreated(final Message<Seed> message) {

        return message.ack();
    }

    /**
     * Reacts on the deletion of seeds. Deletes associated polls.
     *
     * @param message {@link Message} containing information of the deleted seed.
     * @return CompletionStage
     */
    @Transactional
    @Blocking
    @Incoming("seed-deleted")
    public CompletionStage<Void> onSeedDeleted(final Message<String> message) {
        final int result = this.pollService.deleteBySeed(message.getPayload());
        Log.debugv("Deleted {0} poll(s) of seed {1}", result, message.getPayload());

        return message.ack();
    }

    @Inject
    public void setPollService(PollService pollService) {
        this.pollService = pollService;
    }
}
