package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new DefaultCsvParser();
    private final PrettyPrinter printer = new DefaultPrettyPrinter();

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
    void normalisedScoreOf101ReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,2,101");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void emptyPlayerIdReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine(",Alice,g1,Chess,2,50");

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertNotNull(error);
    }

    @Test
    void emptyGameIdReturnsParseError() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,,Chess,2,50");

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

    @Test
    void whitespicePaddedFieldsParseTrimmedValues() {
        Result<ScoreRecord, ParseError> result = parser.parseLine(" p1 , Alice , g1 , Chess , 2 , 50 ");

        assertInstanceOf(Result.Ok.class, result);

        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals(new Player("p1", "Alice"), record.player());
        assertEquals(new GameEntry("g1", "Chess", 2, 50), record.gameEntry());
    }

    @Test
    void parseLinesReturnsFirstParseErrorOnMixedValidInvalidList() {
        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(
                List.of("p1,Alice,g1,Chess,2,50", "invalid"));

        assertInstanceOf(Result.Err.class, result);

        ParseError error = ((Result.Err<List<ScoreRecord>, ParseError>) result).error();
        assertEquals("invalid", error.offendingLine());
    }

    @Test
    void parseLinesReturnsAllScoreRecordsInOrderForValidList() {
        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(
                List.of("p1,Alice,g1,Chess,2,50", "p2,Bob,g2,Poker,3,70"));

        assertInstanceOf(Result.Ok.class, result);

        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) result).value();

        assertEquals(2, records.size());
        assertEquals(new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
                records.get(0));
        assertEquals(new ScoreRecord(new Player("p2", "Bob"), new GameEntry("g2", "Poker", 3, 70)),
                records.get(1));
    }

    @Test
    void prettyPrinterFormatsScoreRecordAsSixFieldCsv() {
        ScoreRecord record = new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50));

        String result = printer.print(record);

        assertEquals("p1,Alice,g1,Chess,2,50", result);
    }
}
