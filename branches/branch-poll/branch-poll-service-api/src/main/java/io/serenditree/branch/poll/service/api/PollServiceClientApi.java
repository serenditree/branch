package io.serenditree.branch.poll.service.api;

import io.serenditree.branch.poll.model.entities.Poll;

import java.util.List;

public interface PollServiceClientApi {
    List<Poll> create(List<Poll> polls);

    void deleteBySeed(String seedId);
}
