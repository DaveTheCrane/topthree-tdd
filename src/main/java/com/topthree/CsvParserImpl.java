package com.topthree;

import java.util.List;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }
        // Trim fields before validation
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        // Validate player id is not empty/blank
        if (fields[0].isEmpty()) {
            return new Result.Err<>(new ParseError("Player id must not be blank", csvLine));
        }

        // Validate game id is not empty/blank
        if (fields[2].isEmpty()) {
            return new Result.Err<>(new ParseError("Game id must not be blank", csvLine));
        }

        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid integer for hours-played: " + fields[4], csvLine));
        }
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid integer for normalised-score: " + fields[5], csvLine));
        }
        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Result.Err<>(new ParseError("normalised-score out of range [1,100]: " + normalisedScore, csvLine));
        }
        Player player = new Player(fields[0], fields[1]);
        GameEntry gameEntry = new GameEntry(
                fields[2],
                fields[3],
                hoursPlayed,
                normalisedScore
        );
        return new Result.Ok<>(new ScoreRecord(player, gameEntry));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new java.util.ArrayList<>();
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
                return new Result.Err<>(err.error());
            }
            records.add(((Result.Ok<ScoreRecord, ParseError>) result).value());
        }
        return new Result.Ok<>(records);
    }
}
