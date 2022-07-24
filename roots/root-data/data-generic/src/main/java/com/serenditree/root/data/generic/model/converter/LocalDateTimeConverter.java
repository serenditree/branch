package com.serenditree.root.data.generic.model.converter;

import javax.persistence.AttributeConverter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Converts LocalDateTime to Date.
 * Needs to be in the same package as the target repository.
 * @deprecated
 */
@Deprecated(forRemoval = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

    @Override
    public Date convertToDatabaseColumn(LocalDateTime localDateTime) {

        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Date date) {

        return date != null ? LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : null;
    }
}
