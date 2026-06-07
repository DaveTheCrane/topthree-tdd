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
}
