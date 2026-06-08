package com.topthree;

import java.util.List;

public class DefaultCsvParser implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length != 6) {
            return new Result.Err<>(new ParseError("Expected 6 fields but got " + fields.length, csvLine));
        }
        String playerId = fields[0];
        String playerName = fields[1];
        String gameId = fields[2];
        String gameName = fields[3];
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid hours-played: not an integer", csvLine));
        }
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid normalised-score: not an integer", csvLine));
        }
        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Result.Err<>(new ParseError("Normalised score must be between 1 and 100", csvLine));
        }

        Player player = new Player(playerId, playerName);
        GameEntry gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        ScoreRecord record = new ScoreRecord(player, gameEntry);
        return new Result.Ok<>(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
