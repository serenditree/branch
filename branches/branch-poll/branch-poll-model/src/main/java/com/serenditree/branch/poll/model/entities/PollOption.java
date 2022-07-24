package com.serenditree.branch.poll.model.entities;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

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

    private String text;

    private Integer votes;

    /**
     * Empty constructor required by JPA.
     */
    public PollOption() {
    }

    /**
     * Convenience constructor.
     *
     * @param text  Description of the option/choice.
     * @param votes Votes for the option/choice.
     */
    public PollOption(String text, Integer votes) {
        this.text = text;
        this.votes = votes;
    }

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
