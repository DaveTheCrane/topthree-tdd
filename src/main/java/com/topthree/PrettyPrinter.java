package com.topthree;

/**
 * Formats a Score_Record back into canonical CSV.
 * Lives alongside CsvParser in the parsing layer.
 */
public interface PrettyPrinter {

    /**
     * Formats a ScoreRecord as a six-field comma-separated string.
     * No leading/trailing whitespace around fields.
     */
    String print(ScoreRecord record);
}
