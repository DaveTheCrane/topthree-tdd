package com.topthree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new CsvParserImpl();

    @Test
    void validSixFieldLineParsesToCorrectScoreRecord() {
        String line = "AB1234-7643,BigDaveTheViking,G-134,Plumbers versus dinosaurs in small cars,3,34";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        Player expectedPlayer = new Player("AB1234-7643", "BigDaveTheViking");
        GameEntry expectedGame = new GameEntry("G-134", "Plumbers versus dinosaurs in small cars", 3, 34);
        ScoreRecord expectedRecord = new ScoreRecord(expectedPlayer, expectedGame);

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(expectedRecord, ((Result.Ok<ScoreRecord, ParseError>) result).value());
    }

    @Test
    void fewerThanSixFieldsReturnsParseError() {
        String line = "p1,Alice,g1,Chess,3";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void moreThanSixFieldsReturnsParseError() {
        String line = "p1,Alice,g1,Chess,3,34,extraField";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void nonIntegerHoursPlayedReturnsParseError() {
        String line = "p1,Alice,g1,Chess,abc,34";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void nonIntegerNormalisedScoreReturnsParseError() {
        String line = "p1,Alice,g1,Chess,3,1.5";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void normalisedScoreZeroReturnsParseError() {
        String line = "p1,Alice,g1,Chess,3,0";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void normalisedScore101ReturnsParseError() {
        String line = "p1,Alice,g1,Chess,3,101";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void emptyPlayerIdReturnsParseError() {
        String line = " ,Alice,g1,Chess,3,34";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void emptyGameIdReturnsParseError() {
        String line = "p1,Alice, ,Chess,3,34";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void fieldsWithWhitespaceParseTrimmed() {
        String line = " p1 , Alice , g1 , Chess , 2 , 50 ";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        Player expectedPlayer = new Player("p1", "Alice");
        GameEntry expectedGame = new GameEntry("g1", "Chess", 2, 50);
        ScoreRecord expectedRecord = new ScoreRecord(expectedPlayer, expectedGame);

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(expectedRecord, ((Result.Ok<ScoreRecord, ParseError>) result).value());
    }
}
