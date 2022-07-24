package com.serenditree.root.data.generic.model.entities;

import com.serenditree.root.etc.maple.Maple;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Superclass of all entities for a convenient implementation of global features.
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Prints the object in pretty JSON format.
     *
     * @return JSON String.
     */
    @Override
    public String toString() {
        return Maple.prettyJson(this);
    }
}
