package com.serenditree.branch.poll.wind;

import com.serenditree.branch.poll.service.PollService;
import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.log.interceptor.LoggedMessageInterceptor;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * Reacts on the creation an deletion of seeds.
 */
@ApplicationScoped
@Logged(binding = LoggedMessageInterceptor.class)
public class IncomingWind {

    private static final Logger LOGGER = Logger.getLogger(IncomingWind.class.getName());

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
        LOGGER.fine(() -> "Deleted " + result + " poll(s) of seed " + message.getPayload());

        return message.ack();
    }

    @Inject
    public void setPollService(PollService pollService) {
        this.pollService = pollService;
    }
}
