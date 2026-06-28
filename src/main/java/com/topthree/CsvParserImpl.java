package com.topthree;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CsvParser interface.
 * STUB: Returns null - needs implementation.
 */
public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
