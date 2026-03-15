package com.serenditree.leaf.poll.config;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.service.api.PollServiceApi;
import com.serenditree.fence.AbstractFenceDecorator;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import java.util.List;

@Decorator
@Priority(Interceptor.Priority.APPLICATION + 100)
@Dependent
public abstract class PollFence extends AbstractFenceDecorator implements PollServiceApi {

    protected PollServiceApi pollService;

    @Override
    public Integer deleteBySeed(String seedId) {
        List<Poll> pollsToDelete = this.pollService.retrieveBySeed(seedId);
        int deleted = this.pollService.deleteBySeed(seedId);

        Log.debugv("Deleting fence records for polls of seed {0}", seedId);
        pollsToDelete.forEach(
            poll -> this.authorizationRepository.deleteFenceRecordsByEntity(poll.getId().toString())
        );

        return deleted;
    }

    @Inject
    public void setPollService(@Delegate PollServiceApi pollService) {
        this.pollService = pollService;
    }
}
