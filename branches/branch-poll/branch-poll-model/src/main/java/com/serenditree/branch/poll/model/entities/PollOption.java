package com.serenditree.branch.poll.model.entities;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * An entity representing an option users can vote for.
 */
@Entity
public class PollOption extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pollId", referencedColumnName = "id")
    @JsonbTransient
    private Poll poll;

    @NotNull
    private String text;

    @NotNull
    private Integer votes = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
