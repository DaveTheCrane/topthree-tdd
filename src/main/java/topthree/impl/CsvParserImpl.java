package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.List;

import static topthree.models.Result.Err;
import static topthree.models.Result.Ok;

public class CsvParserImpl implements CsvParser {

    @Override
    public Result<ScoreRecord, ParseError> parseLine(String line) {
        String[] fields = line.split(",");
        if (fields.length != 6) {
            return new Err<>(new ParseError("Expected 6 fields"));
        }

        String playerId = fields[0].trim();
        String playerName = fields[1].trim();
        String gameId = fields[2].trim();
        String gameName = fields[3].trim();
        String hoursPlayedStr = fields[4].trim();
        String normalisedScoreStr = fields[5].trim();

        int hoursPlayed;
        try {
            hoursPlayed = Integer.parseInt(hoursPlayedStr);
        } catch (NumberFormatException e) {
            return new Err<>(new ParseError("For input string: \"" + hoursPlayedStr + "\""));
        }

        int normalisedScore;
        try {
            normalisedScore = Integer.parseInt(normalisedScoreStr);
        } catch (NumberFormatException e) {
            return new Err<>(new ParseError("For input string: \"" + normalisedScoreStr + "\""));
        }

        ScoreRecord record = new ScoreRecord(playerId, playerName, gameId, gameName, hoursPlayed, normalisedScore);
        return new Ok<>(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> lines) {
        return new Ok<>(List.of());
    }
}
