package com.topthree;

import java.util.List;

public class DefaultCsvParser implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid hours-played value: " + fields[4], csvLine));
        }
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid normalised-score value: " + fields[5], csvLine));
        }
        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Result.Err<>(new ParseError("Normalised score out of range [1,100]: " + normalisedScore, csvLine));
        }
        if (fields[0].trim().isEmpty()) {
            return new Result.Err<>(new ParseError("Player id is empty", csvLine));
        }
        Player player = new Player(fields[0], fields[1]);
        GameEntry gameEntry = new GameEntry(fields[2], fields[3], hoursPlayed, normalisedScore);
        return new Result.Ok<>(new ScoreRecord(player, gameEntry));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        return null;
    }
}
