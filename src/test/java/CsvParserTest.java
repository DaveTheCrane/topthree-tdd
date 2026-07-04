import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
