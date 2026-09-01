package io.serenditree.root.util.oak;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public class OakDate {

    public static final Pattern DATE_TIME_PATTERN = Pattern.compile("^\\d{4}(-\\d{2}){2}T\\d{2}(:\\d{2}){1,2}$");
    public static final LocalDateTime POSITIVE_INFINITY = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private OakDate() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDateTime today() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
    }
}
