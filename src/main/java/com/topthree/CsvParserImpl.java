package com.topthree;

import java.util.List;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);
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
        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid integer for hours-played: " + fields[4], csvLine));
        }
        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return new Result.Err<>(new ParseError("Invalid integer for normalised-score: " + fields[5], csvLine));
        }
        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Result.Err<>(new ParseError("normalised-score out of range [1,100]: " + normalisedScore, csvLine));
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
