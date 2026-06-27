package com.topthree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new CsvParserImpl();

    @Test
    void fewerThanSixFieldsReturnsParseError() {
        String line = "p1,Alice,g1,Chess,10";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void moreThanSixFieldsReturnsParseError() {
        String line = "p1,Alice,g1,Chess,10,85,extra";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void nonIntegerHoursPlayedReturnsParseError() {
        String line = "p1,Alice,g1,Chess,abc,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void nonIntegerNormalisedScoreReturnsParseError() {
        String line = "p1,Alice,g1,Chess,10,1.5";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void normalisedScoreOfZeroReturnsParseError() {
        String line = "p1,Alice,g1,Chess,10,0";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void normalisedScoreOf101ReturnsParseError() {
        String line = "p1,Alice,g1,Chess,10,101";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void validSixFieldLineParseToCorrectScoreRecord() {
        String line = "p1,Alice,g1,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Ok.class, result);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(10, record.gameEntry().hoursPlayed());
        assertEquals(85, record.gameEntry().normalisedScore());
    }
}
