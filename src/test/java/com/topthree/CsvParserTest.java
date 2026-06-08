package com.topthree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserTest {

    private final CsvParser parser = new DefaultCsvParser();

    @Test
    void validSixFieldLineProducesCorrectScoreRecord() {
        String csvLine = "p1,Alice,g1,Chess,2,50";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(record.player()).isEqualTo(new Player("p1", "Alice"));
        assertThat(record.gameEntry()).isEqualTo(new GameEntry("g1", "Chess", 2, 50));
    }

    @Test
    void fewerThanSixFieldsReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,2";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void moreThanSixFieldsReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,2,50,extra";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }
}
