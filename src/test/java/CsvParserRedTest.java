// TDD Red-Green-Refactor Test
// Task 3.4 Red: Normalised-score outside range [1,100] returns ParseError

import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvParserRangeValidationTest {

    @Test
    void rejectsNormalisedScoreOfZero() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,0";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        Result.Err<ScoreRecord, ParseError> err = (Result.Err<ScoreRecord, ParseError>) result;
        assertEquals("normalised-score out of range [1,100]", err.error().message());
    }

    @Test
    void rejectsNormalisedScoreOfOneHundredOne() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,101";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        Result.Err<ScoreRecord, ParseError> err = (Result.Err<ScoreRecord, ParseError>) result;
        assertEquals("normalised-score out of range [1,100]", err.error().message());
    }
}

