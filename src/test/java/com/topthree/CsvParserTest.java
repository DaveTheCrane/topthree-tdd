package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new DefaultCsvParser();

    @Test
    void trimsWhitespaceFromFields() {
        var result = parser.parseLine(" p1 , Alice , g1 , Chess , 2 , 50 ");

        assertInstanceOf(Result.Ok.class, result);
        var record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(2, record.gameEntry().hoursPlayed());
        assertEquals(50, record.gameEntry().normalisedScore());
    }

    @Test
    void rejectsEmptyPlayerId() {
        var result = parser.parseLine(",Alice,g1,Chess,10,85");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("player"));
    }

    @Test
    void rejectsEmptyGameId() {
        var result = parser.parseLine("p1,Alice,,Chess,10,85");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("game"));
    }

    @Test
    void rejectsNormalisedScoreZero() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,0");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("range"));
    }

    @Test
    void rejectsNormalisedScore101() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,101");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("range"));
    }

    @Test
    void rejectsNonIntegerNormalisedScore() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,1.5");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("normalised-score"));
    }

    @Test
    void rejectsNonIntegerHoursPlayed() {
        var result = parser.parseLine("p1,Alice,g1,Chess,abc,85");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("hours-played"));
    }

    @Test
    void rejectsLineWithFiveFields() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("6"));
    }

    @Test
    void rejectsLineWithSevenFields() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,85,extra");

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertTrue(error.message().contains("6"));
    }

    @Test
    void parsesValidSixFieldCsvLine() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,85");

        assertInstanceOf(Result.Ok.class, result);
        var record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(10, record.gameEntry().hoursPlayed());
        assertEquals(85, record.gameEntry().normalisedScore());
    }
}
