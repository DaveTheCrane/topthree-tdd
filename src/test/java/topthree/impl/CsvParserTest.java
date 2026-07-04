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
    
    @Test
    void parseLine_normalizedScoreZero_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,10,0";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("normalized score must be between 1 and 100"));
    }
    
    @Test
    void parseLine_normalizedScore101_returnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,10,101";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("normalized score must be between 1 and 100"));
    }
    
    @Test
    void parseLine_normalizedScoreAtBoundary1_returnsOk() {
        String csvLine = "p1,Alice,g1,Chess,10,1";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Ok);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals(1, record.normalizedScore());
    }
    
    @Test
    void parseLine_normalizedScoreAtBoundary100_returnsOk() {
        String csvLine = "p1,Alice,g1,Chess,10,100";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Ok);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals(100, record.normalizedScore());
    }
    
    @Test
    void parseLine_emptyPlayerId_returnsParseError() {
        String csvLine = ",Alice,g1,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("player id cannot be empty"));
    }
    
    @Test
    void parseLine_blankPlayerId_returnsParseError() {
        String csvLine = "   ,Alice,g1,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("player id cannot be empty"));
    }
    
    @Test
    void parseLine_emptyGameId_returnsParseError() {
        String csvLine = "p1,Alice,,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("game id cannot be empty"));
    }
    
    @Test
    void parseLine_blankGameId_returnsParseError() {
        String csvLine = "p1,Alice,   ,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("game id cannot be empty"));
    }
    
    @Test
    void parseLine_withWhitespace_returnsTrimmedValues() {
        String csvLine = "  p1  ,  Alice  ,  g1  ,  Chess  ,  10  ,  85  ";
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
    void parseLines_twoValidLines_returnsTwoRecordsInOrder() {
        String line1 = "p1,Alice,g1,Chess,10,85";
        String line2 = "p2,Bob,g2,Checkers,5,90";
        Result<java.util.List<ScoreRecord>, ParseError> result = parser.parseLines(java.util.List.of(line1, line2));
        
        assertTrue(result instanceof Result.Ok);
        java.util.List<ScoreRecord> records = ((Result.Ok<java.util.List<ScoreRecord>, ParseError>) result).value();
        
        assertEquals(2, records.size());
        assertEquals("p1", records.get(0).playerId());
        assertEquals("p2", records.get(1).playerId());
    }
    
    @Test
    void parseLines_secondLineInvalid_returnsParseErrorForSecondLine() {
        String line1 = "p1,Alice,g1,Chess,10,85";
        String line2 = "p2,Bob,g2,Checkers,invalid,90"; // invalid hours
        Result<java.util.List<ScoreRecord>, ParseError> result = parser.parseLines(java.util.List.of(line1, line2));
        
        assertTrue(result instanceof Result.Err);
        ParseError error = ((Result.Err<java.util.List<ScoreRecord>, ParseError>) result).error();
        assertTrue(error.message().contains("Invalid number format"));
    }
}