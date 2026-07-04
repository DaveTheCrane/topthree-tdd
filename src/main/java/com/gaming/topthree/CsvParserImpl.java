package com.gaming.topthree;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link CsvParser} implementation.
 */
public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);

        if (fields.length != 6) {
            return Result.err(new ParseError(
                    "Expected 6 fields but found " + fields.length, csvLine));
        }

        String playerId = fields[0].trim();
        String playerName = fields[1].trim();
        String gameId = fields[2].trim();
        String gameName = fields[3].trim();

        if (playerId.isBlank()) {
            return Result.err(new ParseError("player-id is empty", csvLine));
        }
        if (gameId.isBlank()) {
            return Result.err(new ParseError("game-id is empty", csvLine));
        }

        Result<Integer, ParseError> hours = parseIntField(fields[4].trim(), "hours-played", csvLine);
        if (hours instanceof Result.Err<Integer, ParseError> err) {
            return Result.err(err.error());
        }
        int hoursPlayed = ((Result.Ok<Integer, ParseError>) hours).value();

        Result<Integer, ParseError> score = parseIntField(fields[5].trim(), "normalised-score", csvLine);
        if (score instanceof Result.Err<Integer, ParseError> err) {
            return Result.err(err.error());
        }
        int normalisedScore = ((Result.Ok<Integer, ParseError>) score).value();

        if (normalisedScore < 1 || normalisedScore > 100) {
            return Result.err(new ParseError(
                    "normalised-score out of range [1,100]: " + normalisedScore, csvLine));
        }

        ScoreRecord record = new ScoreRecord(
                new Player(playerId, playerName),
                new GameEntry(gameId, gameName, hoursPlayed, normalisedScore)
        );
        return Result.ok(record);
    }

    private static Result<Integer, ParseError> parseIntField(String value, String fieldName, String csvLine) {
        try {
            return Result.ok(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Result.err(new ParseError(
                    fieldName + " is not an integer: " + value, csvLine));
        }
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new ArrayList<>();
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
                return Result.err(err.error());
            }
            records.add(((Result.Ok<ScoreRecord, ParseError>) result).value());
        }
        return Result.ok(records);
    }
}
