package com.serenditree.branch.seed.model.entities;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity that represents water, pruning and nubits.
 */
public class Nutrition extends AbstractEntity {

    private int value;

    private LocalDateTime added;

    public Nutrition(int value) {
        this.value = value;
        // Avoid linking nutrition with fence records by time.
        this.added = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
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
