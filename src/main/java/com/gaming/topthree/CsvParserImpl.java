package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link CsvParser} implementation.
 */
public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);

        String playerId = fields[0];
        String playerName = fields[1];
        String gameId = fields[2];
        String gameName = fields[3];
        int hoursPlayed = Integer.parseInt(fields[4]);
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
