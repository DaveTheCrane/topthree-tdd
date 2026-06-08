package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void nonIntegerHoursPlayedReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,abc,50";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void nonIntegerNormalisedScoreReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,2,1.5";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void normalisedScoreOfZeroReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,2,0";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void normalisedScoreOf101ReturnsParseError() {
        String csvLine = "p1,Alice,g1,Chess,2,101";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void emptyPlayerIdReturnsParseError() {
        String csvLine = " ,Alice,g1,Chess,2,50";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void emptyGameIdReturnsParseError() {
        String csvLine = "p1,Alice, ,Chess,2,50";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<ScoreRecord, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(csvLine);
    }

    @Test
    void whitespacePaddedFieldsParseToTrimmedValues() {
        String csvLine = " p1 , Alice , g1 , Chess , 2 , 50 ";

        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Ok.class);
        ScoreRecord record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertThat(record.player()).isEqualTo(new Player("p1", "Alice"));
        assertThat(record.gameEntry()).isEqualTo(new GameEntry("g1", "Chess", 2, 50));
    }

    @Test
    void parseLinesReturnsAllScoreRecordsInOrder() {
        List<String> lines = List.of("p1,Alice,g1,Chess,2,50", "p2,Bob,g2,Go,3,80");

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(lines);

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) result).value();
        assertThat(records).hasSize(2);
        assertThat(records.get(0).player()).isEqualTo(new Player("p1", "Alice"));
        assertThat(records.get(0).gameEntry()).isEqualTo(new GameEntry("g1", "Chess", 2, 50));
        assertThat(records.get(1).player()).isEqualTo(new Player("p2", "Bob"));
        assertThat(records.get(1).gameEntry()).isEqualTo(new GameEntry("g2", "Go", 3, 80));
    }

    @Test
    void parseLinesReturnsFirstParseErrorOnMixedValidInvalidList() {
        String validLine = "p1,Alice,g1,Chess,2,50";
        String invalidLine = "p2,Bob,g2";
        List<String> lines = List.of(validLine, invalidLine);

        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(lines);

        assertThat(result).isInstanceOf(Result.Err.class);
        ParseError error = ((Result.Err<List<ScoreRecord>, ParseError>) result).error();
        assertThat(error.offendingLine()).isEqualTo(invalidLine);
    }
}
