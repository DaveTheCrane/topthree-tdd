package com.topthree;

import java.util.List;

public interface CsvParser {
    Result<ScoreRecord, ParseError> parseLine(String csvLine);
    Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines);
}
