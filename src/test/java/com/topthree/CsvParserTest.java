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
}
