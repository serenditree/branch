package com.serenditree.branch.poll.service;


import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.repository.api.PollRepositoryApi;
import com.serenditree.branch.poll.service.api.PollServiceApi;
import com.serenditree.fence.model.FenceResponse;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Poll service.
 */
@Dependent
public class PollService implements PollServiceApi {

    private PollRepositoryApi pollRepository;

    /**
     * Persists all polls an returns them containing their IDs.
     *
     * @param polls Transfer objects.
     * @return Persisted polls containing their IDs.
     */
    @Override
    public List<Poll> create(List<Poll> polls) {
        List<Poll> createdPolls = new ArrayList<>(polls.size());
        for (Poll poll : polls) {
            this.pollRepository.persist(poll);
            createdPolls.add(poll);
        }

        return createdPolls;
    }

    /**
     * Retrieves a list of polls by seed.
     *
     * @param seedId Id of the seed the poll belongs to.
     * @return Polls associated with the given seed.
     */
    @Override
    public List<Poll> retrieveBySeed(String seedId) {
        return this.pollRepository.retrieveBySeed(seedId);
    }

    /**
     * Increments the vote count for the given option of the given poll.
     *
     * @param pollId   Poll
     * @param optionId Option to vote for.
     * @return {@link FenceResponse} containing the ID of the poll for {@link com.serenditree.fence.model.FenceRecord}
     * creation.
     */
    @Override
    public FenceResponse vote(Long pollId, Long optionId) {
        return this.pollRepository.vote(pollId, optionId);
    }

    /**
     * Deletes all polls associated with the given seed.
     *
     * @param seedId ID of the seed.
     * @return Number of deleted seeds.
     */
    @Override
    public Integer deleteBySeed(String seedId) {
        return this.pollRepository.deleteBySeed(seedId);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setPollRepository(PollRepositoryApi pollRepository) {
        this.pollRepository = pollRepository;
    }
}
