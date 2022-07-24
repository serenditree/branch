package com.serenditree.branch.poll.service.api;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.fence.model.FenceResponse;

import java.util.List;

public interface PollServiceApi {
    List<Poll> create(List<Poll> polls);

    List<Poll> retrieveBySeed(String seedId);

    FenceResponse vote(Long pollId, Long optionId);

    Integer deleteBySeed(String seedId);
}
