package com.topthree;

import org.junit.jupiter.api.Test;

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
}
