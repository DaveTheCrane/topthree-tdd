package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserProperties {

    private final CsvParser parser = new DefaultCsvParser();

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    // **Validates: Requirements 1.1**
    @Property(tries = 1000)
    void validCsvLineParsesToCorrectFields(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), String.valueOf(normalisedScore));

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(record.player().playerId()).isEqualTo(playerId.trim());
        assertThat(record.player().playerName()).isEqualTo(playerName.trim());
        assertThat(record.gameEntry().gameId()).isEqualTo(gameId.trim());
        assertThat(record.gameEntry().gameName()).isEqualTo(gameName.trim());
        assertThat(record.gameEntry().hoursPlayed()).isEqualTo(hoursPlayed);
        assertThat(record.gameEntry().normalisedScore()).isEqualTo(normalisedScore);
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    // **Validates: Requirements 1.2**
    @Property(tries = 1000)
    void outOfRangeNormalisedScoreReturnsError(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll int hoursPlayed,
            @ForAll("outOfRangeScore") int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), String.valueOf(normalisedScore));

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<Integer> outOfRangeScore() {
        return Arbitraries.oneOf(
                Arbitraries.integers().lessOrEqual(0),
                Arbitraries.integers().greaterOrEqual(101)
        );
    }

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
    // **Validates: Requirements 1.3**
    @Property(tries = 1000)
    void nonIntegerHoursPlayedReturnsError(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll("nonIntegerString") String hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                hoursPlayed, String.valueOf(normalisedScore));

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<String> nonIntegerString() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .filter(s -> !s.contains(","))
                .filter(s -> {
                    try {
                        Integer.parseInt(s.trim());
                        return false;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                });
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns an error
    // **Validates: Requirements 1.4**
    @Property(tries = 1000)
    void nonIntegerNormalisedScoreReturnsError(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll int hoursPlayed,
            @ForAll("nonIntegerString") String normalisedScore
    ) {
        String csvLine = String.join(",", playerId, playerName, gameId, gameName,
                String.valueOf(hoursPlayed), normalisedScore);

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    // **Validates: Requirements 1.5, 1.6**
    @Property(tries = 1000)
    void wrongFieldCountReturnsError(
            @ForAll("wrongFieldCount") int fieldCount
    ) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            fields.add("field" + i);
        }
        String csvLine = String.join(",", fields);

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<Integer> wrongFieldCount() {
        return Arbitraries.integers().between(1, 10).filter(n -> n != 6);
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    // **Validates: Requirements 1.10**
    @Property(tries = 1000)
    void whitespaceTrimming(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore,
            @ForAll("whitespace") String ws1Pre,
            @ForAll("whitespace") String ws1Suf,
            @ForAll("whitespace") String ws2Pre,
            @ForAll("whitespace") String ws2Suf,
            @ForAll("whitespace") String ws3Pre,
            @ForAll("whitespace") String ws3Suf,
            @ForAll("whitespace") String ws4Pre,
            @ForAll("whitespace") String ws4Suf,
            @ForAll("whitespace") String ws5Pre,
            @ForAll("whitespace") String ws5Suf,
            @ForAll("whitespace") String ws6Pre,
            @ForAll("whitespace") String ws6Suf
    ) {
        String hoursStr = String.valueOf(hoursPlayed);
        String scoreStr = String.valueOf(normalisedScore);

        String unpaddedLine = String.join(",", playerId, playerName, gameId, gameName, hoursStr, scoreStr);
        String paddedLine = String.join(",",
                ws1Pre + playerId + ws1Suf,
                ws2Pre + playerName + ws2Suf,
                ws3Pre + gameId + ws3Suf,
                ws4Pre + gameName + ws4Suf,
                ws5Pre + hoursStr + ws5Suf,
                ws6Pre + scoreStr + ws6Suf
        );

        Result<ScoreRecord, ParseError> unpaddedResult = parser.parseLine(unpaddedLine);
        Result<ScoreRecord, ParseError> paddedResult = parser.parseLine(paddedLine);

        assertThat(unpaddedResult).isInstanceOf(Result.Ok.class);
        assertThat(paddedResult).isEqualTo(unpaddedResult);
    }

    @Provide
    Arbitrary<String> whitespace() {
        return Arbitraries.strings().withChars(' ', '\t').ofMaxLength(3);
    }

    // Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
    // **Validates: Requirements 1.11, 1.12**
    @Property(tries = 1000)
    void parsePrintParseRoundTrip(
            @ForAll("nonEmptyNoComma") String playerId,
            @ForAll("noComma") String playerName,
            @ForAll("nonEmptyNoComma") String gameId,
            @ForAll("noComma") String gameName,
            @ForAll int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int normalisedScore
    ) {
        Player player = new Player(playerId.trim(), playerName.trim());
        GameEntry gameEntry = new GameEntry(gameId.trim(), gameName.trim(), hoursPlayed, normalisedScore);
        ScoreRecord original = new ScoreRecord(player, gameEntry);

        PrettyPrinter printer = new DefaultPrettyPrinter();
        CsvParser csvParser = new DefaultCsvParser();

        String csv = printer.print(original);
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csv);

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord parsed = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(parsed).isEqualTo(original);
    }

    @Provide
    Arbitrary<String> nonEmptyNoComma() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(20)
                .filter(s -> !s.contains(","))
                .filter(s -> !s.trim().isEmpty());
    }

    @Provide
    Arbitrary<String> noComma() {
        return Arbitraries.strings()
                .ofMaxLength(20)
                .filter(s -> !s.contains(","));
    }
}
