package topthree.interfaces;

import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.List;

public interface CsvParser {

    Result<ScoreRecord, ParseError> parseLine(String line);

    Result<List<ScoreRecord>, ParseError> parseLines(List<String> lines);
}
