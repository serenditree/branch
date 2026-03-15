package io.serenditree.branch.poll.repository;


import io.serenditree.branch.poll.model.entities.Poll;
import io.serenditree.branch.poll.repository.api.PollRepositoryApi;
import io.serenditree.fence.model.FenceResponse;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.List;
import java.util.UUID;

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
     * @param seedId ID of the seed the poll belongs to.
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
     * @return {@link FenceResponse} containing the ID of the poll for {@link io.serenditree.fence.model.FenceRecord}
     * creation.
     */
    @Override
    public FenceResponse vote(UUID pollId, Long optionId) {
        int result = this.getEntityManager().createNamedQuery(Poll.VOTE)
            .setParameter(Poll.OPTION_REFERENCE, optionId)
            .executeUpdate();

        return result == 1 ? new FenceResponse(pollId) : new FenceResponse(null);
    }

    /**
     * Deletes all polls associated with the given seed.
     *
     * @param seedId ID of the seed.
     * @return Number of deleted polls.
     */
    @Override
    public Integer deleteBySeed(String seedId) {

        return this.getEntityManager().createNamedQuery(Poll.DELETE_BY_SEED)
            .setParameter(Poll.SEED_REFERENCE, seedId)
            .executeUpdate();
    }

    /**
     * Deletes all polls associated with the given user.
     *
     * @param userId ID of the user.
     * @return Number of deleted polls.
     */
    @Override
    public Integer deleteByUser(Long userId) {

        return this.getEntityManager().createNamedQuery(Poll.DELETE_BY_USER)
            .setParameter(Poll.USER_REFERENCE, userId)
            .executeUpdate();
    }
}
