// TDD Red-Green-Refactor Test
// Task 3.1 Red: Valid six-field CSV line parses to correct ScoreRecord

import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvParserRedTest {

    @Test
    void validSixFieldLineParsesToCorrectScoreRecord() {
        CsvParser parser = new CsvParserImpl();
        String line = "p1,Alice,g1,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertTrue(result.isOk());
        Result.Ok<ScoreRecord, ParseError> ok = (Result.Ok<ScoreRecord, ParseError>) result;
        ScoreRecord record = ok.value();
        assertEquals("p1", record.playerId());
        assertEquals("Alice", record.playerName());
        assertEquals("g1", record.gameId());
        assertEquals("Chess", record.gameName());
        assertEquals(10, record.hoursPlayed());
        assertEquals(85, record.normalisedScore());
    }
}
