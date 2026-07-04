package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link CsvParser} implementation.
 */
public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);

        if (fields.length != 6) {
            return Result.err(new ParseError(
                    "Expected 6 fields but found " + fields.length, csvLine));
        }

        String playerId = fields[0];
        String playerName = fields[1];
        String gameId = fields[2];
        String gameName = fields[3];

        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(fields[4]);
        } catch (NumberFormatException e) {
            return Result.err(new ParseError(
                    "hours-played is not an integer: " + fields[4], csvLine));
        }

        int normalisedScore = Integer.parseInt(fields[5]);

        ScoreRecord record = new ScoreRecord(
                new Player(playerId, playerName),
                new GameEntry(gameId, gameName, hoursPlayed, normalisedScore)
        );
        return Result.ok(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
