package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;
import java.util.List;

public class CsvParserImpl implements CsvParser {
    
    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);
        
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields, got " + fields.length));
        }
        
        try {
            String playerId = fields[0].trim();
            String playerName = fields[1].trim();
            String gameId = fields[2].trim();
            String gameName = fields[3].trim();
            int hoursPlayed = Integer.parseInt(fields[4].trim());
            int normalizedScore = Integer.parseInt(fields[5].trim());
            
            if (playerId.isEmpty()) {
                return new Result.Err<>(new ParseError("player id cannot be empty"));
            }
            
            if (gameId.isEmpty()) {
                return new Result.Err<>(new ParseError("game id cannot be empty"));
            }
            
            if (normalizedScore < 1 || normalizedScore > 100) {
                return new Result.Err<>(new ParseError(
                    "normalized score must be between 1 and 100, got " + normalizedScore
                ));
            }
            
            return new Result.Ok<>(new ScoreRecord(
                playerId,
                playerName,
                gameId,
                gameName,
                hoursPlayed,
                normalizedScore
            ));
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid number format: " + e.getMessage()));
        }
    }
    
    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new java.util.ArrayList<>();
        
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result instanceof Result.Err) {
                return new Result.Err<>(((Result.Err<ScoreRecord, ParseError>) result).error());
            }
            records.add(((Result.Ok<ScoreRecord, ParseError>) result).value());
        }
        
        return new Result.Ok<>(records);
    }
}