package com.topthree;

import java.util.List;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid integer for hours-played: " + fields[4], csvLine));
        }
        Player player = new Player(fields[0], fields[1]);
        GameEntry gameEntry = new GameEntry(
                fields[2],
                fields[3],
                hoursPlayed,
                Integer.parseInt(fields[5])
        );
        return new Result.Ok<>(new ScoreRecord(player, gameEntry));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        return new Result.Err<>(new ParseError("Not implemented", ""));
    }
}
