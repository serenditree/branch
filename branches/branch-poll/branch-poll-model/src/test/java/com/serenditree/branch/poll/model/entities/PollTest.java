package com.serenditree.branch.poll.model.entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PollTest {

    @Test
    void referenceOptionsTest() {
        Poll poll = new Poll("poll");
        poll.setOptions(List.of(new PollOption(), new PollOption()));

        assertThat(poll.getOptions(), everyItem(notNullValue()));
        poll.getOptions().forEach(pollOption -> assertEquals("poll", pollOption.getPoll().getTitle()));
        assertEquals(2, poll.getOptions().size());
    }
}
