package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

import static topthree.models.Result.Err;
import static topthree.models.Result.Ok;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String line) {
        String[] fields = line.split(",");
        if (fields.length != 6) {
            return new Err<>(new ParseError("Expected 6 fields, got " + fields.length));
        }

        String playerId = fields[0].trim();
        String playerName = fields[1].trim();
        String gameId = fields[2].trim();
        String gameName = fields[3].trim();
        String hoursPlayedStr = fields[4].trim();
        String normalisedScoreStr = fields[5].trim();

        if (playerId.isBlank()) {
            return new Err<>(new ParseError("player-id is blank"));
        }
        if (gameId.isBlank()) {
            return new Err<>(new ParseError("game-id is blank"));
        }

        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(hoursPlayedStr);
        } catch (NumberFormatException e) {
            return new Err<>(new ParseError("hours-played is not an integer"));
        }

        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(normalisedScoreStr);
        } catch (NumberFormatException e) {
            return new Err<>(new ParseError("normalised-score is not an integer"));
        }

        if (normalisedScore < 1 || normalisedScore > 100) {
            return new Err<>(new ParseError("normalised-score out of range [1,100]"));
        }

        ScoreRecord record = new ScoreRecord(playerId, playerName, gameId, gameName, hoursPlayed, normalisedScore);
        return new Ok<>(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> lines) {
        List<ScoreRecord> results = new ArrayList<>();
        for (String line : lines) {
            Result<ScoreRecord, ParseError> result = parseLine(line);
            if (!result.isOk()) {
                return new Err<>(result.error());
            }
            results.add(result.get());
        }
        return new Ok<>(results);
    }
}
