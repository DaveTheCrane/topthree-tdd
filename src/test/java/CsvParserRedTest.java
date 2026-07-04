// TDD Red-Green-Refactor Test
// Task 3.2 Red: Lines with wrong field count return ParseError

import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvParserFieldCountTest {

    @Test
    void rejectsLinesWithFiveFields() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("Expected 6 fields", result.error().message());
    }

    @Test
    void rejectsLinesWithSevenFields() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,85,extra";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("Expected 6 fields", result.error().message());
    }
}

