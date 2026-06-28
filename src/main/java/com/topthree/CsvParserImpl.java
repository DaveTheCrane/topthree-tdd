package com.topthree;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CsvParser for parsing CSV records into typed ScoreRecord objects.
 * Follows hexagonal architecture: pure function with no I/O side effects.
 */
public class CsvParserImpl implements CsvParser {
    
    private static final int EXPECTED_FIELD_COUNT = 6;
    private static final int SCORE_MIN = 1;
    private static final int SCORE_MAX = 100;

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return new Result.Err<>(new ParseError("CSV line cannot be null or empty", csvLine));
        }

        String[] fields = csvLine.split(",", -1);
        
        // Check field count
        if (fields.length != EXPECTED_FIELD_COUNT) {
            return new Result.Err<>(new ParseError(
                String.format("Expected %d fields, but got %d", EXPECTED_FIELD_COUNT, fields.length),
                csvLine
            ));
        }

        // Trim all fields
        String[] trimmedFields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            trimmedFields[i] = fields[i].trim();
        }

        // Extract and validate fields
        String playerId = trimmedFields[0];
        String playerName = trimmedFields[1];
        String gameId = trimmedFields[2];
        String gameName = trimmedFields[3];
        String hoursPlayedStr = trimmedFields[4];
        String normalisedScoreStr = trimmedFields[5];

        // Validate player id is not empty
        if (playerId.isEmpty()) {
            return new Result.Err<>(new ParseError("Player id cannot be empty", csvLine));
        }

        // Validate game id is not empty
        if (gameId.isEmpty()) {
            return new Result.Err<>(new ParseError("Game id cannot be empty", csvLine));
        }

        // Parse hours played as integer
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(hoursPlayedStr);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError(
                String.format("Hours played must be an integer, got: %s", hoursPlayedStr),
                csvLine
            ));
        }

        // Parse normalised score as integer
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(normalisedScoreStr);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError(
                String.format("Normalised score must be an integer, got: %s", normalisedScoreStr),
                csvLine
            ));
        }

        // Validate normalised score range [1, 100]
        if (normalisedScore < SCORE_MIN || normalisedScore > SCORE_MAX) {
            return new Result.Err<>(new ParseError(
                String.format("Normalised score must be between %d and %d, got: %d", 
                    SCORE_MIN, SCORE_MAX, normalisedScore),
                csvLine
            ));
        }

        // Construct result
        Player player = new Player(playerId, playerName);
        GameEntry gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        ScoreRecord scoreRecord = new ScoreRecord(player, gameEntry);

        return new Result.Ok<>(scoreRecord);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        if (csvLines == null || csvLines.isEmpty()) {
            return new Result.Ok<>(new ArrayList<>());
        }

        List<ScoreRecord> records = new ArrayList<>();
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
                return new Result.Err<>(err.error());
            }
            if (result instanceof Result.Ok<ScoreRecord, ParseError> ok) {
                records.add(ok.value());
            }
        }

        return new Result.Ok<>(records);
    }
}
