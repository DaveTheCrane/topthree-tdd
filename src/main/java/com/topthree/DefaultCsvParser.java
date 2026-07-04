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

        String playerId = fields[0];
        String playerName = fields[1];
        String gameId = fields[2];
        String gameName = fields[3];

        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Invalid hours-played: " + fields[4], csvLine));
        }

        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError("Invalid normalised-score: " + fields[5], csvLine));
        }

        var player = new Player(playerId, playerName);
        var gameEntry = new GameEntry(gameId, gameName, hoursPlayed, normalisedScore);
        return Result.ok(new ScoreRecord(player, gameEntry));
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
