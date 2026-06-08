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
