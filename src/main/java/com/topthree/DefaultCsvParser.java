package com.topthree;

import com.topthree.model.*;

import java.util.List;

public class DefaultCsvParser implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);
        if (fields.length != 6) {
            return Result.err(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }

        String playerId = fields[0].trim();
        String playerName = fields[1].trim();
        String gameId = fields[2].trim();
        String gameName = fields[3].trim();

        if (playerId.isBlank()) {
            return Result.err(new ParseError("Empty player-id", csvLine));
        }
        if (gameId.isBlank()) {
            return Result.err(new ParseError("Empty game-id", csvLine));
        }

        var hoursResult = parseIntField(fields[4].trim(), "hours-played", csvLine);
        if (hoursResult instanceof Result.Err<Integer, ParseError> err) {
            return Result.err(err.error());
        }
        int hoursPlayed = ((Result.Ok<Integer, ParseError>) hoursResult).value();

        var scoreResult = parseIntField(fields[5].trim(), "normalised-score", csvLine);
        if (scoreResult instanceof Result.Err<Integer, ParseError> err) {
            return Result.err(err.error());
        }
        int normalisedScore = ((Result.Ok<Integer, ParseError>) scoreResult).value();

        if (normalisedScore < 1 || normalisedScore > 100) {
            return Result.err(new ParseError("Normalised-score out of range [1,100]: " + normalisedScore, csvLine));
        }

        var player = new Player(playerId, playerName);
        var gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        return Result.ok(new ScoreRecord(player, gameEntry));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        var records = new java.util.ArrayList<ScoreRecord>();
        for (String line : csvLines) {
            var result = parseLine(line);
            if (result instanceof Result.Ok<ScoreRecord, ParseError> ok) {
                records.add(ok.value());
            } else if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
                return Result.err(err.error());
            }
        }
        return Result.ok(records);
    }

    private Result<Integer, ParseError> parseIntField(String value, String fieldName, String csvLine) {
        try {
            return Result.ok(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Invalid " + fieldName + ": " + value, csvLine));
        }
    }
}
