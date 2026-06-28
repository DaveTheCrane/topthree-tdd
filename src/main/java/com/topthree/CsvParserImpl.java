package com.topthree;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CsvParser interface.
 */
public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");
        
        // Check field count - must be exactly 6
        if (fields.length < 6) {
            return Result.err(new ParseError("Too few fields: expected 6, got " + fields.length, csvLine));
        }
        if (fields.length > 6) {
            return Result.err(new ParseError("Too many fields: expected 6, got " + fields.length, csvLine));
        }
        
        // Trim all fields
        String playerId = fields[0].trim();
        String playerName = fields[1].trim();
        String gameId = fields[2].trim();
        String gameName = fields[3].trim();
        String hoursPlayedStr = fields[4].trim();
        String normalisedScoreStr = fields[5].trim();
        
        // Validate non-empty player id
        if (playerId.isEmpty()) {
            return Result.err(new ParseError("Player ID is empty", csvLine));
        }
        
        // Validate non-empty game id
        if (gameId.isEmpty()) {
            return Result.err(new ParseError("Game ID is empty", csvLine));
        }
        
        // Parse hours played - must be an integer
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(hoursPlayedStr);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Hours played is not an integer: " + hoursPlayedStr, csvLine));
        }
        
        // Parse normalised score - must be an integer
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(normalisedScoreStr);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Normalised score is not an integer: " + normalisedScoreStr, csvLine));
        }
        
        // Validate normalised score range [1, 100]
        if (normalisedScore < 1 || normalisedScore > 100) {
            return Result.err(new ParseError("Normalised score must be between 1 and 100, got: " + normalisedScore, csvLine));
        }
        
        Player player = new Player(playerId, playerName);
        GameEntry gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        ScoreRecord scoreRecord = new ScoreRecord(player, gameEntry);
        
        return Result.ok(scoreRecord);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new ArrayList<>();
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result.isErr()) {
                return Result.err(result.getError());
            }
            records.add(result.get());
        }
        return Result.ok(records);
    }
}
