package com.topthree.component;

import com.topthree.model.*;

import java.util.ArrayList;
import java.util.List;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");

        if (fields.length != 6) {
            return Result.err(new ParseError("CSV line must have exactly 6 fields", csvLine));
        }

        // Trim all fields
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        // Check for empty player id and game id
        if (fields[0].isBlank()) {
            return Result.err(new ParseError("Player ID cannot be empty", csvLine));
        }
        if (fields[2].isBlank()) {
            return Result.err(new ParseError("Game ID cannot be empty", csvLine));
        }

        // Parse hours played
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Hours played must be an integer", csvLine));
        }

        // Parse normalised score
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Normalised score must be an integer", csvLine));
        }

        // Validate normalised score range
        if (normalisedScore < 1 || normalisedScore > 100) {
            return Result.err(new ParseError("Normalised score must be between 1 and 100", csvLine));
        }

        Player player = new Player(fields[0], fields[1]);
        GameEntry gameEntry = new GameEntry(fields[2], fields[3], hoursPlayed, normalisedScore);
        ScoreRecord record = new ScoreRecord(player, gameEntry);

        return Result.ok(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        List<ScoreRecord> records = new ArrayList<>();
        for (String line : csvLines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
                return Result.err(err.error());
            } else if (result instanceof Result.Ok<ScoreRecord, ParseError> ok) {
                records.add(ok.value());
            }
        }
        return Result.ok(records);
    }
}
