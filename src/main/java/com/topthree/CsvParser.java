package com.topthree;

import java.util.List;

/**
 * Converts a list of raw CSV strings into a list of Score_Record objects.
 */
public interface CsvParser {

    /**
     * Parses a single CSV line into a Score_Record.
     * Returns ParseError if the line is malformed or values are out of range.
     */
    Result<ScoreRecord, ParseError> parseLine(String csvLine);

    /**
     * Parses an ordered list of CSV lines.
     * Returns the first ParseError encountered, or the full list of ScoreRecords.
     */
    Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines);
}
