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
        ScoreRecord record = new ScoreRecord(
                fields[0].trim(),
                fields[1].trim(),
                fields[2].trim(),
                fields[3].trim(),
                Integer.parseInt(fields[4].trim()),
                Integer.parseInt(fields[5].trim())
        );
        return new Ok<>(record);
    }

    @Override
    public Result<List<ScoreRecord>, ParseError> parseLines(List<String> lines) {
        return new Ok<>(List.of());
    }
}
