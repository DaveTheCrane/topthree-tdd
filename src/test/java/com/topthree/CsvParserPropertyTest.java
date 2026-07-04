package com.topthree;

import com.topthree.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserPropertyTest {

    private final CsvParser parser = new DefaultCsvParser();
    private final PrettyPrinter printer = new DefaultPrettyPrinter();

    // --- Generators ---

    @Provide
    Arbitrary<String> nonBlankIds() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(10)
                .filter(s -> !s.isBlank() && !s.contains(","));
    }

    @Provide
    Arbitrary<String> playerNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(15)
                .filter(s -> !s.contains(","));
    }

    @Provide
    Arbitrary<Integer> validHours() {
        return Arbitraries.integers().between(1, 1000);
    }

    @Provide
    Arbitrary<Integer> validScores() {
        return Arbitraries.integers().between(1, 100);
    }

    @Provide
    Arbitrary<ScoreRecord> validScoreRecords() {
        return Combinators.combine(
                nonBlankIds(),
                playerNames(),
                nonBlankIds(),
                playerNames(),
                validHours(),
                validScores()
        ).as((pid, pname, gid, gname, hours, score) ->
                new ScoreRecord(
                        new Player(pid, pname),
                        new GameEntry(gid, gname, hours, score)
                )
        );
    }

    @Provide
    Arbitrary<String> validCsvLines() {
        return validScoreRecords().map(printer::print);
    }

    // --- Property Tests ---

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    @Property(tries = 1000)
    void validCsvLineParsesToCorrectFields(
            @ForAll("nonBlankIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("nonBlankIds") String gameId,
            @ForAll("playerNames") String gameName,
            @ForAll("validHours") int hours,
            @ForAll("validScores") int score
    ) {
        String csv = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hours + "," + score;
        var result = parser.parseLine(csv);

        assertInstanceOf(Result.Ok.class, result);
        var record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals(playerId, record.player().playerId());
        assertEquals(playerName, record.player().playerName());
        assertEquals(gameId, record.gameEntry().gameId());
        assertEquals(gameName, record.gameEntry().gameName());
        assertEquals(hours, record.gameEntry().hoursPlayed());
        assertEquals(score, record.gameEntry().normalisedScore());
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns error
    @Property(tries = 1000)
    void outOfRangeNormalisedScoreReturnsError(
            @ForAll("nonBlankIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("nonBlankIds") String gameId,
            @ForAll("playerNames") String gameName,
            @ForAll("validHours") int hours,
            @ForAll @IntRange(min = -1000, max = 1000) int score
    ) {
        Assume.that(score < 1 || score > 100);

        String csv = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hours + "," + score;
        var result = parser.parseLine(csv);

        assertInstanceOf(Result.Err.class, result);
    }

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns error
    @Property(tries = 1000)
    void nonIntegerHoursPlayedReturnsError(
            @ForAll("nonBlankIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("nonBlankIds") String gameId,
            @ForAll("playerNames") String gameName,
            @ForAll("validScores") int score
    ) {
        String[] badHours = {"abc", "1.5", "", "three", "1e2", "null"};
        for (String bad : badHours) {
            String csv = playerId + "," + playerName + "," + gameId + "," + gameName + "," + bad + "," + score;
            var result = parser.parseLine(csv);
            assertInstanceOf(Result.Err.class, result, "Expected error for hours-played: " + bad);
        }
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns error
    @Property(tries = 1000)
    void nonIntegerNormalisedScoreReturnsError(
            @ForAll("nonBlankIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("nonBlankIds") String gameId,
            @ForAll("playerNames") String gameName,
            @ForAll("validHours") int hours
    ) {
        String[] badScores = {"abc", "1.5", "", "fifty", "1e1", "null"};
        for (String bad : badScores) {
            String csv = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hours + "," + bad;
            var result = parser.parseLine(csv);
            assertInstanceOf(Result.Err.class, result, "Expected error for normalised-score: " + bad);
        }
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns error
    @Property(tries = 1000)
    void wrongFieldCountReturnsError(
            @ForAll @IntRange(min = 1, max = 10) int fieldCount
    ) {
        Assume.that(fieldCount != 6);

        StringBuilder csv = new StringBuilder("field1");
        for (int i = 1; i < fieldCount; i++) {
            csv.append(",field").append(i + 1);
        }
        var result = parser.parseLine(csv.toString());

        assertInstanceOf(Result.Err.class, result);
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    @Property(tries = 1000)
    void whitespaceTrimPreservesFieldValues(
            @ForAll("nonBlankIds") String playerId,
            @ForAll("playerNames") String playerName,
            @ForAll("nonBlankIds") String gameId,
            @ForAll("playerNames") String gameName,
            @ForAll("validHours") int hours,
            @ForAll("validScores") int score
    ) {
        String csvClean = playerId + "," + playerName + "," + gameId + "," + gameName + "," + hours + "," + score;
        String csvPadded = " " + playerId + " , " + playerName + " , " + gameId + " , " + gameName + " , " + hours + " , " + score + " ";

        var cleanResult = parser.parseLine(csvClean);
        var paddedResult = parser.parseLine(csvPadded);

        assertInstanceOf(Result.Ok.class, cleanResult);
        assertInstanceOf(Result.Ok.class, paddedResult);
        assertEquals(
                ((Result.Ok<ScoreRecord, ParseError>) cleanResult).value(),
                ((Result.Ok<ScoreRecord, ParseError>) paddedResult).value()
        );
    }

    // Feature: top-three-high-scores, Property 7: Parse -> print -> parse round trip
    @Property(tries = 1000)
    void roundTripProperty(@ForAll("validScoreRecords") ScoreRecord record) {
        var csv = printer.print(record);
        var result = parser.parseLine(csv);

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(record, ((Result.Ok<ScoreRecord, ParseError>) result).value());
    }
}
