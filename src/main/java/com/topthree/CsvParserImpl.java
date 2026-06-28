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
        
        Player player = new Player(fields[0].trim(), fields[1].trim());
        GameEntry gameEntry = new GameEntry(fields[2].trim(), fields[3].trim(), 
                Integer.parseInt(fields[4].trim()), Integer.parseInt(fields[5].trim()));
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
