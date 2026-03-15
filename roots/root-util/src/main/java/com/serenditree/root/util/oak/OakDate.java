package com.serenditree.root.util.oak;

import java.time.LocalDateTime;

public class OakDate {
    private OakDate() {
    }

    public static final LocalDateTime POSITIVE_INFINITY = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000);
    public static final LocalDateTime NEGATIVE_INFINITY = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
}
