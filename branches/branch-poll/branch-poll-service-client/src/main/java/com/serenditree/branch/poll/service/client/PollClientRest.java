package com.serenditree.branch.poll.service.client;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.service.api.PollServiceClientApi;
import com.serenditree.root.rest.client.AbstractClientRest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * REST Poll client to interact with the poll service.
 *
 * @param <T> {@link Poll}
 */
@Dependent
public class PollClientRest<T> extends AbstractClientRest<Poll> implements PollServiceClientApi {

    @Inject
    @ConfigProperty(name = "serenditree.endpoint.poll", defaultValue = "branch-poll:8080/api/v1")
    String endpoint;

    /**
     * Persists the given polls.
     *
     * @param polls Polls to persist.
     * @return List of persisted polls.
     */
    @Override
    public List<Poll> create(List<Poll> polls) {
        return this.post(polls, "http://" + this.endpoint + "/internal/poll/create");
    }

    /**
     * Deletes all polls associated with the given seed.
     *
     * @param seedId ID of the associated seed.
     */
    @Override
    public void deleteBySeed(String seedId) {
        this.delete(seedId, "http://" + this.endpoint + "/internal/poll/delete/seed");
    }
}
