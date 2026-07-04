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

    @Test
    void rejectsNonIntegerHoursPlayed() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,abc,85");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNonIntegerNormalisedScore() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10,1.5");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNormalisedScoreBelowRange() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10,0");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNormalisedScoreAboveRange() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,g1,Chess,10,101");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsEmptyPlayerId() {
        Result<ScoreRecord, ParseError> result = parser.parseLine(",Alice,g1,Chess,10,85");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsEmptyGameId() {
        Result<ScoreRecord, ParseError> result = parser.parseLine("p1,Alice,,Chess,10,85");

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void trimsWhitespaceFromFields() {
        Result<ScoreRecord, ParseError> result = parser.parseLine(" p1 , Alice , g1 , Chess , 2 , 50 ");

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(record.player().playerId()).isEqualTo("p1");
        assertThat(record.player().playerName()).isEqualTo("Alice");
        assertThat(record.gameEntry().gameId()).isEqualTo("g1");
        assertThat(record.gameEntry().gameName()).isEqualTo("Chess");
        assertThat(record.gameEntry().hoursPlayed()).isEqualTo(2);
        assertThat(record.gameEntry().normalisedScore()).isEqualTo(50);
    }
}
