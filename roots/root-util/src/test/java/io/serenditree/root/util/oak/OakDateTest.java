package io.serenditree.root.util.oak;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OakDateTest {

    @Test
    void infinityTest() {
        assertTrue(OakDate.POSITIVE_INFINITY.isAfter(LocalDateTime.now()));
        assertEquals("9999-12-31T23:59:59", OakDate.POSITIVE_INFINITY.toString());
    }

    @Test
    void formatTest() {
        assertTrue(OakDate.DATE_TIME_PATTERN.matcher(OakDate.now().toString()).matches());
        assertTrue(OakDate.DATE_TIME_PATTERN.matcher(OakDate.today().toString()).matches());
        assertTrue(OakDate.today().toString().endsWith("00:00"));
    }
}
