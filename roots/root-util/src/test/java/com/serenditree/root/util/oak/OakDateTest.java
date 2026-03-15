package com.serenditree.root.util.oak;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OakDateTest {

    @Test
    void infinityTest() {
        assertTrue(OakDate.POSITIVE_INFINITY.isAfter(LocalDateTime.now()));
        assertTrue(OakDate.NEGATIVE_INFINITY.isBefore(LocalDateTime.now()));
    }
}
