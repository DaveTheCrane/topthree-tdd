package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
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
    void parseLinesReturnsFirstParseErrorOnMixedValidInvalidList() {
        List<String> lines = List.of(
                "p1,Alice,g1,Chess,10,85",
                "bad line"
        );

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(lines);

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<List<ScoreRecord>, ParseError>) result).error();
        assertEquals("bad line", error.offendingLine());
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

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    // **Validates: Requirements 1.1**
    @Property(tries = 1000)
    void validCsvLineParsesToCorrectFields(
            @ForAll("nonEmptyNoCommaStrings") String playerId,
            @ForAll("noCommaStrings") String playerName,
            @ForAll("nonEmptyNoCommaStrings") String gameId,
            @ForAll("noCommaStrings") String gameName,
            @ForAll @IntRange(min = Integer.MIN_VALUE, max = Integer.MAX_VALUE) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        String csvLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hoursPlayed + "," + normalisedScore;

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Ok.class, result);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals(playerId.trim(), record.player().playerId());
        assertEquals(playerName.trim(), record.player().playerName());
        assertEquals(gameId.trim(), record.gameEntry().gameId());
        assertEquals(gameName.trim(), record.gameEntry().gameName());
        assertEquals(hoursPlayed, record.gameEntry().hoursPlayed());
        assertEquals(normalisedScore, record.gameEntry().normalisedScore());
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    // **Validates: Requirements 1.2**
    @Property(tries = 1000)
    void outOfRangeNormalisedScoreReturnsError(
            @ForAll("nonEmptyNoCommaStrings") String playerId,
            @ForAll("noCommaStrings") String playerName,
            @ForAll("nonEmptyNoCommaStrings") String gameId,
            @ForAll("noCommaStrings") String gameName,
            @ForAll @IntRange(min = 0, max = Integer.MAX_VALUE) int hoursPlayed,
            @ForAll("outOfRangeScores") int normalisedScore
    ) {
        String csvLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hoursPlayed + "," + normalisedScore;

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<Integer> outOfRangeScores() {
        return Arbitraries.oneOf(
                Arbitraries.integers().lessOrEqual(0),
                Arbitraries.integers().greaterOrEqual(101)
        );
    }

    @Provide
    Arbitrary<String> nonEmptyNoCommaStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(20)
                .alpha()
                .numeric()
                .withChars('_', '-', '.')
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> noCommaStrings() {
        return Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(20)
                .alpha()
                .numeric()
                .withChars('_', '-', '.', ' ');
    }
}
