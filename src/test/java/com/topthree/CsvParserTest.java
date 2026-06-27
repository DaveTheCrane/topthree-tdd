package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

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
    void emptyPlayerIdReturnsParseError() {
        String line = "  ,Alice,g1,Chess,10,85";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertEquals(line, error.offendingLine());
    }

    @Test
    void emptyGameIdReturnsParseError() {
        String line = "p1,Alice, ,Chess,10,85";

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

    @Test
    void whitespacePaddedFieldsParsToTrimmedValues() {
        String line = " p1 , Alice , g1 , Chess , 2 , 50 ";

        Result<ScoreRecord, ParseError> result = parser.parseLine(line);

        assertInstanceOf(Result.Ok.class, result);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(2, record.gameEntry().hoursPlayed());
        assertEquals(50, record.gameEntry().normalisedScore());
    }

    @Test
    void parseLinesReturnsAllScoreRecordsInOrderForValidList() {
        List<String> lines = List.of(
                "p1,Alice,g1,Chess,10,85",
                "p2,Bob,g2,Poker,5,70"
        );

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(lines);

        assertInstanceOf(Result.Ok.class, result);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) result).value();

        assertEquals(2, records.size());

        assertEquals("p1", records.get(0).player().playerId());
        assertEquals("Alice", records.get(0).player().playerName());
        assertEquals("g1", records.get(0).gameEntry().gameId());
        assertEquals("Chess", records.get(0).gameEntry().gameName());
        assertEquals(10, records.get(0).gameEntry().hoursPlayed());
        assertEquals(85, records.get(0).gameEntry().normalisedScore());

        assertEquals("p2", records.get(1).player().playerId());
        assertEquals("Bob", records.get(1).player().playerName());
        assertEquals("g2", records.get(1).gameEntry().gameId());
        assertEquals("Poker", records.get(1).gameEntry().gameName());
        assertEquals(5, records.get(1).gameEntry().hoursPlayed());
        assertEquals(70, records.get(1).gameEntry().normalisedScore());
    }
}
