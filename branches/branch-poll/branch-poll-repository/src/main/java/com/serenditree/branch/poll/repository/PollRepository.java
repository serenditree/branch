package com.serenditree.branch.poll.repository;


import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.repository.api.PollRepositoryApi;
import com.serenditree.fence.model.FenceResponse;
import org.eclipse.microprofile.faulttolerance.Retry;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.util.List;

/**
 * Poll repository.
 */
@Dependent
@Retry(
    abortOn = {
        EntityExistsException.class,
        EntityNotFoundException.class,
        NonUniqueResultException.class,
        NoResultException.class
    }
)
public class PollRepository implements PollRepositoryApi {

    /**
     * Retrieves a list of polls by seed.
     *
     * @param seedId Id of the seed the poll belongs to.
     * @return Polls associated with the given seed.
     */
    @Override
    public List<Poll> retrieveBySeed(String seedId) {

        return this.getEntityManager().createNamedQuery(Poll.RETRIEVE_BY_SEED, Poll.class)
            .setParameter(Poll.SEED_REFERENCE, seedId)
            .getResultList();
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
        int result = this.getEntityManager().createNamedQuery(Poll.VOTE)
            .setParameter(Poll.OPTION_REFERENCE, optionId)
            .executeUpdate();

        return result == 1 ? new FenceResponse(pollId) : new FenceResponse(null);
    }

    /**
     * Deletes all polls associated with the given seed.
     *
     * @param seedId ID of the seed.
     * @return Number of deleted seeds.
     */
    @Override
    public Integer deleteBySeed(String seedId) {

        return this.getEntityManager().createNamedQuery(Poll.DELETE_BY_SEED)
            .setParameter(Poll.SEED_REFERENCE, seedId)
            .executeUpdate();
    }
}
