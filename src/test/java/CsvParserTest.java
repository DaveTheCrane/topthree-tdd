import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.interfaces.PrettyPrinter;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Feature: top-three-high-scores
class CsvParserTest {

    @Test
    void parsesValidSixFieldCsvLine() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertTrue(result.isOk());
        ScoreRecord record = result.get();
        assertEquals("p1", record.playerId());
        assertEquals("Alice", record.playerName());
        assertEquals("g1", record.gameId());
        assertEquals("Chess", record.gameName());
        assertEquals(10, record.hoursPlayed());
        assertEquals(85, record.normalisedScore());
    }

    @Test
    void rejectsLinesWithFiveFields() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("Expected 6 fields, got 5", result.error().message());
    }

    @Test
    void rejectsLinesWithSevenFields() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,85,extra";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("Expected 6 fields, got 7", result.error().message());
    }

    @Test
    void rejectsNonIntegerHoursPlayed() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,abc,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("hours-played is not an integer", result.error().message());
    }

    @Test
    void rejectsNonIntegerNormalisedScore() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,1.5";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("normalised-score is not an integer", result.error().message());
    }

    @Test
    void rejectsNormalisedScoreOutsideRange() {
        CsvParser parser = new CsvParserImpl();

        // Score = 0
        Result<ScoreRecord, ParseError> result1 = parser.parseLine("p1,Alice,g1,Chess,10,0");
        assertFalse(result1.isOk());
        assertEquals("normalised-score out of range [1,100]", result1.error().message());

        // Score = 101
        Result<ScoreRecord, ParseError> result2 = parser.parseLine("p1,Alice,g1,Chess,10,101");
        assertFalse(result2.isOk());
        assertEquals("normalised-score out of range [1,100]", result2.error().message());
    }

    @Test
    void rejectsEmptyPlayerId() {
        CsvParser parser = new CsvParserImpl();
        String line = ",Alice,g1,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("player-id is blank", result.error().message());
    }

    @Test
    void rejectsEmptyGameId() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertFalse(result.isOk());
        assertEquals("game-id is blank", result.error().message());
    }

    @Test
    void trimsWhitespaceFromFields() {
        CsvParser parser = new CsvParserImpl();
        String line = " p1 , Alice , g1 , Chess , 10 , 85 ";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertTrue(result.isOk());
        ScoreRecord record = result.get();
        assertEquals("p1", record.playerId());
        assertEquals("Alice", record.playerName());
        assertEquals("g1", record.gameId());
        assertEquals("Chess", record.gameName());
        assertEquals(10, record.hoursPlayed());
        assertEquals(85, record.normalisedScore());
    }

    @Test
    void parseLinesHappyPathAndShortCircuitOnError() {
        CsvParser parser = new CsvParserImpl();

        // Happy path: two valid lines
        Result<List<ScoreRecord>, ParseError> result1 = parser.parseLines(java.util.List.of(
            "p1,Alice,g1,Chess,10,85",
            "p2,Bob,g2,Poker,5,90"
        ));
        assertTrue(result1.isOk());
        assertEquals(2, result1.get().size());

        // Error case: second line is invalid
        Result<List<ScoreRecord>, ParseError> result2 = parser.parseLines(java.util.List.of(
            "p1,Alice,g1,Chess,10,85",
            "p2,Bob,g2,Poker,abc,90"
        ));
        assertFalse(result2.isOk());
    }

    @Test
    void prettyPrinterFormatsScoreRecordAsCsv() {
        CsvParser parser = new CsvParserImpl();
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 10, 85);

        String output = parser.print(record);

        assertEquals("p1,Alice,g1,Chess,10,85", output);
    }
}
