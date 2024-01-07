package com.serenditree.branch.poll.model.entities;

import com.serenditree.fence.model.AbstractTimestampedFenceEntity;
import com.serenditree.fence.model.api.FenceEntity;
import jakarta.persistence.*;

import java.util.List;

/**
 * An entity which represents a poll with title/topic/text and references a list of options users can vote for.
 */
@Entity
@Table(
    indexes = {
        @Index(name = Poll.SEED_REFERENCE, columnList = Poll.SEED_REFERENCE)
    }
)
@NamedQuery(
    name = Poll.RETRIEVE_BY_SEED,
    query = "SELECT p " +
            "FROM Poll p " +
            "WHERE p.seedId = :" + Poll.SEED_REFERENCE
)
@NamedQuery(
    name = Poll.VOTE,
    query = "UPDATE PollOption p " +
            "SET p.votes = p.votes + 1 " +
            "WHERE p.id = :" + Poll.OPTION_REFERENCE
)
@NamedQuery(
    name = Poll.DELETE_BY_SEED,
    query = "DELETE FROM Poll " +
            "WHERE seedId = :" + Poll.SEED_REFERENCE
)
public class Poll extends AbstractTimestampedFenceEntity<Long> implements FenceEntity<Long> {

    public static final String USER_REFERENCE = "userId";
    public static final String SEED_REFERENCE = "seedId";
    public static final String OPTION_REFERENCE = "optionId";

    public static final String RETRIEVE_BY_SEED = "Poll.retrieveBySeed";
    public static final String VOTE = "Poll.vote";
    public static final String DELETE_BY_SEED = "Poll.deleteBySeed";

    /**
     * Empty constructor required by JPA.
     */
    public Poll() {
    }

    /**
     * Convenience constructor.
     *
     * @param title Title of the poll.
     */
    public Poll(String title) {
        this.title = title;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = USER_REFERENCE)
    private Long userId;

    @Column(name = SEED_REFERENCE)
    private String seedId;

    private String title;

    @OneToMany(
        mappedBy = "poll",
        fetch = FetchType.EAGER,
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private List<PollOption> options;

    /**
     * Sets cross-references between {@link Poll} and {@link PollOption} which are required by JPA.
     *
     * @param options Options to set.
     * @return Options with {@link Poll} set.
     */
    private List<PollOption> referenceOptions(List<PollOption> options) {
        for (PollOption option : options) {
            option.setPoll(this);
        }

        return options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long owner) {
        this.userId = owner;
    }

    public String getSeedId() {
        return seedId;
    }

    public void setSeedId(String seedId) {
        this.seedId = seedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PollOption> getOptions() {
        return options;
    }

    public void setOptions(List<PollOption> options) {
        this.options = this.referenceOptions(options);
    }
}
