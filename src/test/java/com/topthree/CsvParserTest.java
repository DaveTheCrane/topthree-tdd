package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new CsvParserImpl();
    private final PrettyPrinter prettyPrinter = new PrettyPrinterImpl();

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

    @Test
    void parseLinesReturnsFirstParseErrorOnMixedList() {
        String validLine = "p1,Alice,g1,Chess,5,80";
        String invalidLine = "p2,Bob,g2";

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(List.of(validLine, invalidLine));

        assertInstanceOf(Result.Err.class, result);
        ParseError error = ((Result.Err<List<ScoreRecord>, ParseError>) result).error();
        assertEquals(invalidLine, error.offendingLine());
    }

    @Test
    void parseLinesReturnsAllRecordsInOrder() {
        String line1 = "p1,Alice,g1,Chess,5,80";
        String line2 = "p2,Bob,g2,Go,10,95";

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(List.of(line1, line2));

        ScoreRecord expected1 = new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 5, 80));
        ScoreRecord expected2 = new ScoreRecord(new Player("p2", "Bob"), new GameEntry("g2", "Go", 10, 95));

        assertInstanceOf(Result.Ok.class, result);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) result).value();
        assertEquals(List.of(expected1, expected2), records);
    }

    @Test
    void prettyPrinterFormatsScoreRecordAsCsv() {
        ScoreRecord record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 3, 50)
        );

        String result = prettyPrinter.print(record);

        assertEquals("p1,Alice,g1,Chess,3,50", result);
    }

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    // Validates: Requirements 1.1
    @Property(tries = 1000)
    void validCsvLineParsesToCorrectFields(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("gameNames") String gameName,
            @ForAll @IntRange(min = 0, max = 10000) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), String.valueOf(normalisedScore));

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
    // Validates: Requirements 1.2
    @Property(tries = 1000)
    void outOfRangeNormalisedScoreReturnsError(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("gameNames") String gameName,
            @ForAll @IntRange(min = 0, max = 10000) int hoursPlayed,
            @ForAll("outOfRangeScores") int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), String.valueOf(normalisedScore));

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
    // Validates: Requirements 1.3
    @Property(tries = 1000)
    void nonIntegerHoursPlayedReturnsParseError(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("gameNames") String gameName,
            @ForAll("nonIntegerStrings") String hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                hoursPlayed, String.valueOf(normalisedScore));

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    // Validates: Requirements 1.5, 1.6
    @Property(tries = 1000)
    void wrongFieldCountReturnsError(
            @ForAll("wrongFieldCountLines") String csvLine
    ) {
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    // Validates: Requirements 1.10
    @Property(tries = 1000)
    void whitespaceTrimmingPreservesFieldValues(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("gameNames") String gameName,
            @ForAll @IntRange(min = 0, max = 10000) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore,
            @ForAll("whitespacePadding") String pad1,
            @ForAll("whitespacePadding") String pad2,
            @ForAll("whitespacePadding") String pad3,
            @ForAll("whitespacePadding") String pad4,
            @ForAll("whitespacePadding") String pad5,
            @ForAll("whitespacePadding") String pad6
    ) {
        String hoursStr = String.valueOf(hoursPlayed);
        String scoreStr = String.valueOf(normalisedScore);

        String unpadded = String.join(",", playerId, playerName, gameId, gameName, hoursStr, scoreStr);
        String padded = String.join(",",
                pad1 + playerId + pad1,
                pad2 + playerName + pad2,
                pad3 + gameId + pad3,
                pad4 + gameName + pad4,
                pad5 + hoursStr + pad5,
                pad6 + scoreStr + pad6);

        Result<ScoreRecord, ParseError> unpaddedResult = parser.parseLine(unpadded);
        Result<ScoreRecord, ParseError> paddedResult = parser.parseLine(padded);

        assertInstanceOf(Result.Ok.class, unpaddedResult);
        assertInstanceOf(Result.Ok.class, paddedResult);

        ScoreRecord unpaddedRecord = ((Result.Ok<ScoreRecord, ParseError>) unpaddedResult).value();
        ScoreRecord paddedRecord = ((Result.Ok<ScoreRecord, ParseError>) paddedResult).value();

        assertEquals(unpaddedRecord, paddedRecord);
    }

    // Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
    // Validates: Requirements 1.11, 1.12
    @Property(tries = 1000)
    void parsePrintParseRoundTrip(
            @ForAll("validScoreRecords") ScoreRecord record
    ) {
        String csvString = prettyPrinter.print(record);
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvString);

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(record, ((Result.Ok<ScoreRecord, ParseError>) result).value());
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns an error
    // Validates: Requirements 1.4
    @Property(tries = 1000)
    void nonIntegerNormalisedScoreReturnsParseErrorProperty(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("gameNames") String gameName,
            @ForAll @IntRange(min = 0, max = 10000) int hoursPlayed,
            @ForAll("nonIntegerStrings") String normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), normalisedScore);

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<String> nonIntegerStrings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(10)
                .filter(s -> {
                    try {
                        Integer.parseInt(s);
                        return false;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                });
    }

    @Provide
    Arbitrary<Integer> outOfRangeScores() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-1000, 0),
                Arbitraries.integers().between(101, 1000)
        );
    }

    @Provide
    Arbitrary<String> playerIds() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> playerNames() {
        return Arbitraries.strings()
                .alpha().numeric().withChars(' ', '-', '_')
                .ofMinLength(1).ofMaxLength(30)
                .filter(s -> !s.contains(","));
    }

    @Provide
    Arbitrary<String> gameIds() {
        return Arbitraries.strings()
                .alpha().numeric().withChars('-')
                .ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> gameNames() {
        return Arbitraries.strings()
                .alpha().numeric().withChars(' ', '-', '_')
                .ofMaxLength(40)
                .filter(s -> !s.contains(","));
    }

    @Provide
    Arbitrary<String> whitespacePadding() {
        return Arbitraries.strings()
                .withChars(' ', '\t')
                .ofMinLength(0).ofMaxLength(5);
    }

    @Provide
    Arbitrary<ScoreRecord> validScoreRecords() {
        Arbitrary<String> playerIdArb = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> playerNameArb = Arbitraries.strings()
                .alpha().numeric().withChars('-', '_')
                .ofMinLength(1).ofMaxLength(30)
                .filter(s -> s.equals(s.trim()));
        Arbitrary<String> gameIdArb = Arbitraries.strings()
                .alpha().numeric().withChars('-')
                .ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> gameNameArb = Arbitraries.strings()
                .alpha().numeric().withChars('-', '_')
                .ofMaxLength(40)
                .filter(s -> s.equals(s.trim()));
        Arbitrary<Integer> hoursArb = Arbitraries.integers().between(0, 10000);
        Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

        return Combinators.combine(playerIdArb, playerNameArb, gameIdArb, gameNameArb, hoursArb, scoreArb)
                .as((pid, pname, gid, gname, hours, score) ->
                        new ScoreRecord(
                                new Player(pid, pname),
                                new GameEntry(gid, gname, hours, score)
                        ));
    }

    @Provide
    Arbitrary<String> wrongFieldCountLines() {
        Arbitrary<Integer> fieldCounts = Arbitraries.oneOf(
                Arbitraries.integers().between(1, 5),
                Arbitraries.integers().between(7, 10)
        );
        return fieldCounts.flatMap(count ->
                Arbitraries.strings()
                        .alpha().numeric().withChars('-', '_', ' ')
                        .ofMinLength(1).ofMaxLength(10)
                        .filter(s -> !s.contains(","))
                        .list().ofSize(count)
                        .map(fields -> String.join(",", fields))
        );
    }
}