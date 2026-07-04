package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.models.ParseError;
import topthree.models.Result;
import topthree.models.ScoreRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {
    
    private final CsvParser parser = new CsvParserImpl();
    
    @Test
    void parseLine_validSixFieldCsvLine_returnsScoreRecord() {
        String csvLine = "p1,Alice,g1,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Ok);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        
        assertEquals("p1", record.playerId());
        assertEquals("Alice", record.playerName());
        assertEquals("g1", record.gameId());
        assertEquals("Chess", record.gameName());
        assertEquals(10, record.hoursPlayed());
        assertEquals(85, record.normalizedScore());
    }
    
    @Test
    void parseLine_fiveFields_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,10";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("Expected 6 fields"));
    }
    
    @Test
    void parseLine_sevenFields_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,10,85,extra";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("Expected 6 fields"));
    }
    
    @Test
    void parseLine_nonIntegerHoursPlayed_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,abc,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("Invalid number format"));
    }
    
    @Test
    void parseLine_nonIntegerNormalizedScore_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,10,abc";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("Invalid number format"));
    }
}