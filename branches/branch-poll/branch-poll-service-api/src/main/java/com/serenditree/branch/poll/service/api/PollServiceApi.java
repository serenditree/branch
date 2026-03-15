package com.serenditree.branch.poll.service.api;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.fence.model.FenceResponse;

import java.util.List;
import java.util.UUID;

public interface PollServiceApi {
    List<Poll> create(List<Poll> polls);

    List<Poll> retrieveBySeed(String seedId);

    FenceResponse vote(UUID pollId, Long optionId);

    Integer deleteBySeed(String seedId);
}
