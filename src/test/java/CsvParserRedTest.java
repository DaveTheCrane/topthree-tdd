// TDD Red-Green-Refactor Test
// Task 3.3 Red: Non-integer hours-played and normalised-score return ParseError

import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvParserIntegerValidationTest {

    @Test
    void rejectsNonIntegerHoursPlayed() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,abc,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        Result.Err<ScoreRecord, ParseError> err = (Result.Err<ScoreRecord, ParseError>) result;
        assertEquals("For input string: \"abc\"", err.error().message());
    }

    @Test
    void rejectsNonIntegerNormalisedScore() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,1.5";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        Result.Err<ScoreRecord, ParseError> err = (Result.Err<ScoreRecord, ParseError>) result;
        assertEquals("For input string: \"1.5\"", err.error().message());
    }
}

