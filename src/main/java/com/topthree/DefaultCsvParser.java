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
        int hoursPlayed = Integer.parseInt(fields[4]);
        int normalisedScore = Integer.parseInt(fields[5]);

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
