package com.topthree;

import java.util.ArrayList;
import java.util.List;

public class DefaultCsvParser implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        String playerId = fields[0];
        if (playerId.isEmpty()) {
            return new Result.Err<>(new ParseError("Player id must not be empty", csvLine));
        }
        String playerName = fields[1];
        String gameId = fields[2];
        if (gameId.isEmpty()) {
            return new Result.Err<>(new ParseError("Game id must not be empty", csvLine));
        }
        String gameName = fields[3];

        Result<Integer, ParseError> hoursResult = parseIntField(fields[4], "hours-played", csvLine);
        if (hoursResult instanceof Result.Err<Integer, ParseError> err) {
            return new Result.Err<>(err.error());
        }
        int hoursPlayed = ((Result.Ok<Integer, ParseError>) hoursResult).value();

        Result<Integer, ParseError> scoreResult = parseIntField(fields[5], "normalised-score", csvLine);
        if (scoreResult instanceof Result.Err<Integer, ParseError> err) {
            return new Result.Err<>(err.error());
        }
        int normalisedScore = ((Result.Ok<Integer, ParseError>) scoreResult).value();

        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Result.Err<>(new ParseError("Normalised score must be between 1 and 100", csvLine));
        }

        Player player = new Player(playerId, playerName);
        GameEntry gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        ScoreRecord record = new ScoreRecord(player, gameEntry);
        return new Result.Ok<>(record);
    }

    private Result<Integer, ParseError> parseIntField(String value, String fieldName, String csvLine) {
        try {
            return new Result.Ok<>(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid " + fieldName + ": not an integer", csvLine));
        }
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new ArrayList<>();
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
