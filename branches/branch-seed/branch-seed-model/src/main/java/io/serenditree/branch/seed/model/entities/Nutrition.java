package io.serenditree.branch.seed.model.entities;

import io.serenditree.root.data.generic.model.entities.AbstractEntity;
import io.serenditree.root.util.oak.OakDate;
import jakarta.json.bind.annotation.JsonbProperty;

import java.time.LocalDateTime;

/**
 * Entity that represents water, pruning and nubits.
 */
public class Nutrition extends AbstractEntity {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_ADDED = "added";

    @JsonbProperty(FIELD_VALUE)
    private int value;

    @JsonbProperty(FIELD_ADDED)
    private LocalDateTime added;

    public Nutrition(int value) {
        this.value = value;
        // Avoid linking nutrition with fence records by time.
        this.added = OakDate.today();
    }

    public int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
    }

    public LocalDateTime getAdded() {
        return added;
    }

    void setAdded(LocalDateTime added) {
        this.added = added;
    }
}
