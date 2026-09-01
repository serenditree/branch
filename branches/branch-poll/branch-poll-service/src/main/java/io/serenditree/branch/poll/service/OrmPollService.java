package io.serenditree.branch.poll.service;


import io.serenditree.branch.poll.model.entities.Poll;
import io.serenditree.branch.poll.repository.api.PollRepository;
import io.serenditree.branch.poll.service.api.PollService;
import io.serenditree.fence.model.FenceResponse;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * Poll service.
 */
@Dependent
public class OrmPollService implements PollService {

    private final PollRepository pollRepository;

    @Inject
    public OrmPollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    /**
     * Persists all polls and returns them containing their IDs.
     *
     * @param polls Transfer objects.
     * @return Persisted polls containing their IDs.
     */
    @Override
    public List<Poll> create(List<Poll> polls) {
        this.pollRepository.persist(polls);

        return polls;
    }

    /**
     * Retrieves a list of polls by seed.
     *
     * @param seedId ID of the seed the poll belongs to.
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
     * @return {@link FenceResponse} containing the ID of the poll for {@link io.serenditree.fence.model.FenceRecord}
     * creation.
     */
    @Override
    public FenceResponse vote(UUID pollId, Long optionId) {
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

    /**
     * Deletes all polls associated with the given user.
     *
     * @param userId ID of the user.
     * @return Number of deleted seeds.
     */
    @Override
    public Integer deleteByUser(Long userId) {
        return this.pollRepository.deleteByUser(userId);
    }
}
