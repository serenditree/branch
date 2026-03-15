package io.serenditree.root.test.extension;

/**
 * Enumeration of log levels for test execution.
 */
public enum LogLevels {
    OFF(Integer.MAX_VALUE),
    SEVERE(1000),
    WARNING(900),
    INFO(800),
    CONFIG(700),
    FINE(500),
    FINER(400),
    FINEST(300),
    ALL(Integer.MIN_VALUE);

    private final int level;

    LogLevels(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
