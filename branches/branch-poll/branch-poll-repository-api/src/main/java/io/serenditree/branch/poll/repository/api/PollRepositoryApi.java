package io.serenditree.branch.poll.repository.api;

import io.serenditree.branch.poll.model.entities.Poll;
import io.serenditree.fence.model.FenceResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;
import java.util.UUID;

public interface PollRepositoryApi extends PanacheRepository<Poll> {
    List<Poll> retrieveBySeed(String seedId);

    FenceResponse vote(UUID pollId, Long optionId);

    Integer deleteBySeed(String seedId);

    Integer deleteByUser(Long userId);
}
