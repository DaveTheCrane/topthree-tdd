package com.gaming.topthree;

/**
 * Formats a {@link ScoreRecord} back into canonical CSV.
 */
public interface PrettyPrinter {

    /**
     * Formats a ScoreRecord as a six-field comma-separated string.
     * No leading/trailing whitespace around fields.
     */
    String print(ScoreRecord record);
}
