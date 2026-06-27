package com.topthree;

import java.util.List;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        return new Result.Err<>(new ParseError("Not implemented", csvLine));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        return new Result.Err<>(new ParseError("Not implemented", ""));
    }
}
