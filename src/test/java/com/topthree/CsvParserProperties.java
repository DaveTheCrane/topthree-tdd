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
