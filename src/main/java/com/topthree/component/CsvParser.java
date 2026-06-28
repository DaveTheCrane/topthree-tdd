package com.topthree.component;

import com.topthree.model.ParseError;
import com.topthree.model.Result;
import com.topthree.model.ScoreRecord;

import java.util.List;

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
