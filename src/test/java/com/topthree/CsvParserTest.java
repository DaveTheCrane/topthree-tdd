package com.topthree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new DefaultCsvParser();

    @Test
    void fewerThanSixFieldsReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void moreThanSixFieldsReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2,50,extra");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void nonIntegerHoursPlayedReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,abc,50");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void nonIntegerNormalisedScoreReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2,1.5");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void normalisedScoreOfZeroReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2,0");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void validSixFieldLineProducesCorrectScoreRecord() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2,50");

        assertInstanceOf(Result.Ok.class, result);

        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals(new Player("p1", "Alice"), record.player());
        assertEquals(new GameEntry("g1", "Chess", 2, 50), record.gameEntry());
    }
}
