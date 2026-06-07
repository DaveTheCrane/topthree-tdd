package com.topthree;

public interface PrettyPrinter {
    /**
     * Formats a ScoreRecord as a six-field comma-separated string.
     * No leading/trailing whitespace around fields.
     */
    String print(ScoreRecord record);
}
