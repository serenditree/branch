package io.serenditree.root.data.generic.model.entities;

import io.serenditree.root.util.maple.Maple;
import jakarta.persistence.MappedSuperclass;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.Serial;
import java.io.Serializable;

/**
 * Superclass of all entities for a convenient implementation of global features.
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final boolean LOG_PRETTY = ConfigProvider
        .getConfig()
        .getValue("serenditree.log.pretty", Boolean.class);

    /**
     * Prints the object in pretty JSON format.
     *
     * @return JSON String.
     */
    @Override
    public String toString() {
        return LOG_PRETTY ? Maple.prettyJson(this) : Maple.json(this);
    }
}
