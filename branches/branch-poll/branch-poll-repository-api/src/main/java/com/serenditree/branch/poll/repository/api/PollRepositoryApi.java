package com.serenditree.branch.poll.repository.api;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.fence.model.FenceResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;

public interface PollRepositoryApi extends PanacheRepository<Poll> {
    List<Poll> retrieveBySeed(String seedId);

    FenceResponse vote(Long pollId, Long optionId);

    Integer deleteBySeed(String seedId);
}
