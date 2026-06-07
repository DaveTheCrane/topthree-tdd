package com.topthree;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserPropertyTest {

    private final CsvParser parser = new DefaultCsvParser();

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    // **Validates: Requirements 1.1**
    @Property(tries = 1000)
    void validCsvLineParsesToCorrectFields(
            @ForAll("playerIds") String playerId,
            @ForAll("names") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("names") String gameName,
            @ForAll("hoursPlayed") int hoursPlayed,
            @ForAll("normalisedScores") int normalisedScore
    ) {
        String csvLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hoursPlayed + "," + normalisedScore;

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Ok.class, result);

        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();

        assertEquals(playerId, record.player().playerId());
        assertEquals(playerName, record.player().playerName());
        assertEquals(gameId, record.gameEntry().gameId());
        assertEquals(gameName, record.gameEntry().gameName());
        assertEquals(hoursPlayed, record.gameEntry().hoursPlayed());
        assertEquals(normalisedScore, record.gameEntry().normalisedScore());
    }

    @Provide
    Arbitrary<String> playerIds() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
    }

    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
    }

    @Provide
    Arbitrary<String> gameIds() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
    }

    @Provide
    Arbitrary<Integer> hoursPlayed() {
        return Arbitraries.integers();
    }

    @Provide
    Arbitrary<Integer> normalisedScores() {
        return Arbitraries.integers().between(1, 100);
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    // **Validates: Requirements 1.2**
    @Property(tries = 1000)
    void outOfRangeNormalisedScoreReturnsError(
            @ForAll("playerIds") String playerId,
            @ForAll("names") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("names") String gameName,
            @ForAll("hoursPlayed") int hoursPlayed,
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

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
    // **Validates: Requirements 1.3**
    @Property(tries = 1000)
    void nonIntegerHoursPlayedReturnsError(
            @ForAll("playerIds") String playerId,
            @ForAll("names") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("names") String gameName,
            @ForAll("nonIntegerHoursPlayed") String nonIntegerHours,
            @ForAll("normalisedScores") int normalisedScore
    ) {
        String csvLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + nonIntegerHours + "," + normalisedScore;

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<String> nonIntegerHoursPlayed() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5);
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns an error
    // **Validates: Requirements 1.4**
    @Property(tries = 1000)
    void nonIntegerNormalisedScoreReturnsError(
            @ForAll("playerIds") String playerId,
            @ForAll("names") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("names") String gameName,
            @ForAll("hoursPlayed") int hoursPlayed,
            @ForAll("nonIntegerNormalisedScores") String nonIntegerScore
    ) {
        String csvLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hoursPlayed + "," + nonIntegerScore;

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<String> nonIntegerNormalisedScores() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5);
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    // **Validates: Requirements 1.5, 1.6**
    @Property(tries = 1000)
    void wrongFieldCountReturnsError(
            @ForAll("wrongFieldCountLines") String csvLine
    ) {
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<String> wrongFieldCountLines() {
        return Arbitraries.integers().between(1, 10).filter(n -> n != 6)
                .flatMap(fieldCount ->
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                                .list().ofSize(fieldCount)
                                .map(fields -> String.join(",", fields))
                );
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    // **Validates: Requirements 1.10**
    @Property(tries = 1000)
    void whitespaceTrimPreservesFieldValues(
            @ForAll("playerIds") String playerId,
            @ForAll("names") String playerName,
            @ForAll("gameIds") String gameId,
            @ForAll("names") String gameName,
            @ForAll("hoursPlayed") int hoursPlayed,
            @ForAll("normalisedScores") int normalisedScore,
            @ForAll("whitespace") String ws1,
            @ForAll("whitespace") String ws2,
            @ForAll("whitespace") String ws3,
            @ForAll("whitespace") String ws4,
            @ForAll("whitespace") String ws5,
            @ForAll("whitespace") String ws6
    ) {
        String cleanLine = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hoursPlayed + "," + normalisedScore;
        String paddedLine = ws1 + playerId + ws1 + ","
                + ws2 + playerName + ws2 + ","
                + ws3 + gameId + ws3 + ","
                + ws4 + gameName + ws4 + ","
                + ws5 + hoursPlayed + ws5 + ","
                + ws6 + normalisedScore + ws6;

        Result<ScoreRecord, ParseError> cleanResult = parser.parseLine(cleanLine);
        Result<ScoreRecord, ParseError> paddedResult = parser.parseLine(paddedLine);

        assertInstanceOf(Result.Ok.class, cleanResult);
        assertInstanceOf(Result.Ok.class, paddedResult);

        ScoreRecord cleanRecord = ((Result.Ok<ScoreRecord, ParseError>) cleanResult).value();
        ScoreRecord paddedRecord = ((Result.Ok<ScoreRecord, ParseError>) paddedResult).value();

        assertEquals(cleanRecord.player().playerId(), paddedRecord.player().playerId());
        assertEquals(cleanRecord.player().playerName(), paddedRecord.player().playerName());
        assertEquals(cleanRecord.gameEntry().gameId(), paddedRecord.gameEntry().gameId());
        assertEquals(cleanRecord.gameEntry().gameName(), paddedRecord.gameEntry().gameName());
        assertEquals(cleanRecord.gameEntry().hoursPlayed(), paddedRecord.gameEntry().hoursPlayed());
        assertEquals(cleanRecord.gameEntry().normalisedScore(), paddedRecord.gameEntry().normalisedScore());
    }

    @Provide
    Arbitrary<String> whitespace() {
        return Arbitraries.integers().between(0, 5)
                .map(n -> " ".repeat(n));
    }

    // Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
    // **Validates: Requirements 1.11, 1.12**
    @Property(tries = 1000)
    void parsePrintParseRoundTrip(
            @ForAll("validScoreRecords") ScoreRecord record
    ) {
        PrettyPrinter printer = new DefaultPrettyPrinter();

        String csv = printer.print(record);
        Result<ScoreRecord, ParseError> result = parser.parseLine(csv);

        assertInstanceOf(Result.Ok.class, result);
        ScoreRecord parsed = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals(record, parsed);
    }

    @Provide
    Arbitrary<ScoreRecord> validScoreRecords() {
        Arbitrary<String> playerId = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> playerName = Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
        Arbitrary<String> gameId = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> gameName = Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
        Arbitrary<Integer> hours = Arbitraries.integers();
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);

        return Combinators.combine(playerId, playerName, gameId, gameName, hours, score)
                .as((pid, pname, gid, gname, h, s) ->
                        new ScoreRecord(new Player(pid, pname), new GameEntry(gid, gname, h, s)));
    }
}
