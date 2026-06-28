package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser csvParser = new CsvParserImpl();
    private final PrettyPrinter prettyPrinter = new PrettyPrinterImpl();

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    @Test
    void parseLine_returnsOk_withValidSixFieldLine() {
        String csvLine = "p1,Alice,g1,Chess,2,50";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isOk());
        ScoreRecord record = result.get();
        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(2, record.gameEntry().hoursPlayed());
        assertEquals(50, record.gameEntry().normalisedScore());
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    @Test
    void parseLine_returnsParseError_withFewerThanSixFields() {
        String csvLine = "p1,Alice,g1,Chess,2";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().contains("fields"));
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    @Test
    void parseLine_returnsParseError_withMoreThanSixFields() {
        String csvLine = "p1,Alice,g1,Chess,2,50,extra";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().contains("fields"));
    }

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
    @Test
    void parseLine_returnsParseError_withNonIntegerHoursPlayed() {
        String csvLine = "p1,Alice,g1,Chess,abc,50";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr(), "Expected Err but got: " + result);
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("hours"));
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score returns an error
    @Test
    void parseLine_returnsParseError_withNonIntegerNormalisedScore() {
        String csvLine = "p1,Alice,g1,Chess,2,1.5";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("score"));
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    @Test
    void parseLine_returnsParseError_withNormalisedScoreOf0() {
        String csvLine = "p1,Alice,g1,Chess,2,0";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("score"));
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    @Test
    void parseLine_returnsParseError_withNormalisedScoreOf101() {
        String csvLine = "p1,Alice,g1,Chess,2,101";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("score"));
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    @Test
    void parseLine_trimsWhitespaceFromFields() {
        String csvLine = " p1 , Alice , g1 , Chess , 2 , 50 ";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isOk());
        ScoreRecord record = result.get();
        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(2, record.gameEntry().hoursPlayed());
        assertEquals(50, record.gameEntry().normalisedScore());
    }

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    // Test parseLines - order preservation
    @Test
    void parseLines_preservesOrder() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p2,Bob,g2,Checkers,3,75"
        );
        
        Result<List<ScoreRecord>, ParseError> result = csvParser.parseLines(csvLines);
        
        assertTrue(result.isOk());
        List<ScoreRecord> records = result.get();
        assertEquals(2, records.size());
        assertEquals("p1", records.get(0).player().playerId());
        assertEquals("p2", records.get(1).player().playerId());
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    @Test
    void parseLines_shortCircuitsOnFirstError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p2,Bob,g2,Checkers,abc,75"  // invalid hours
        );
        
        Result<List<ScoreRecord>, ParseError> result = csvParser.parseLines(csvLines);
        
        assertTrue(result.isErr());
    }

    // Feature: top-three-high-scores, Property 7: Parse -> print -> parse round trip
    @Test
    void prettyPrinter_formatsScoreRecordAsCSV() {
        Player player = new Player("p1", "Alice");
        GameEntry game = new GameEntry("g1", "Chess", 2, 50);
        ScoreRecord record = new ScoreRecord(player, game);
        
        String csv = prettyPrinter.print(record);
        
        assertEquals("p1,Alice,g1,Chess,2,50", csv);
    }

    // Feature: top-three-high-scores, Requirement 1.7: Empty player id returns ParseError
    @Test
    void parseLine_returnsParseError_withEmptyPlayerId() {
        String csvLine = ",Alice,g1,Chess,2,50";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("player"));
    }

    // Feature: top-three-high-scores, Requirement 1.8: Empty game id returns ParseError
    @Test
    void parseLine_returnsParseError_withEmptyGameId() {
        String csvLine = "p1,Alice,,Chess,2,50";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertTrue(result.isErr());
        ParseError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("game"));
    }
}
