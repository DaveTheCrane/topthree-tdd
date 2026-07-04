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
            int hoursPlayed = Integer.parseInt(fields[4].trim());
            int normalizedScore = Integer.parseInt(fields[5].trim());
            
            return new Result.Ok<>(new ScoreRecord(
                fields[0].trim(),
                fields[1].trim(),
                fields[2].trim(),
                fields[3].trim(),
                hoursPlayed,
                normalizedScore
            ));
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid number format: " + e.getMessage()));
        }
    }
    
    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        // Will be implemented in a later task
        return new Result.Err<>(new ParseError("Not implemented yet"));
    }
}