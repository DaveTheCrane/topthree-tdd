package com.gaming.topthree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserTest {

    private final CsvParser parser = new CsvParserImpl();

    @Test
    void parsesValidSixFieldLine() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10,85");

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(record.player().playerId()).isEqualTo("p1");
        assertThat(record.player().playerName()).isEqualTo("Alice");
        assertThat(record.gameEntry().gameId()).isEqualTo("g1");
        assertThat(record.gameEntry().gameName()).isEqualTo("Chess");
        assertThat(record.gameEntry().hoursPlayed()).isEqualTo(10);
        assertThat(record.gameEntry().normalisedScore()).isEqualTo(85);
    }

    @Test
    void rejectsLineWithTooFewFields() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsLineWithTooManyFields() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10,85,extra");

        assertThat(result).isInstanceOf(Result.Err.class);
    }
}
