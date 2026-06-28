package com.topthree.component;

import com.topthree.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class CsvParserTest {
    private CsvParser parser;

    @BeforeEach
    void setup() {
        parser = new CsvParserImpl();
    }

    @Test
    void parsesValidSixFieldCsvLine() {
        String csvLine = "p1,Alice,g1,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<ScoreRecord, ParseError> ok = (Result.Ok<ScoreRecord, ParseError>) result;
        ScoreRecord record = ok.value();

        assertThat(record.player().playerId()).isEqualTo("p1");
        assertThat(record.player().playerName()).isEqualTo("Alice");
        assertThat(record.gameEntry().gameId()).isEqualTo("g1");
        assertThat(record.gameEntry().gameName()).isEqualTo("Chess");
        assertThat(record.gameEntry().hoursPlayed()).isEqualTo(10);
        assertThat(record.gameEntry().normalisedScore()).isEqualTo(85);
    }

    @Test
    void rejectsFiveFieldCsvLine() {
        String csvLine = "p1,Alice,g1,Chess,10";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsSevenFieldCsvLine() {
        String csvLine = "p1,Alice,g1,Chess,10,85,extra";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNonIntegerHoursPlayed() {
        String csvLine = "p1,Alice,g1,Chess,abc,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNonIntegerNormalisedScore() {
        String csvLine = "p1,Alice,g1,Chess,10,1.5";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNormalisedScoreZero() {
        String csvLine = "p1,Alice,g1,Chess,10,0";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsNormalisedScore101() {
        String csvLine = "p1,Alice,g1,Chess,10,101";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsEmptyPlayerId() {
        String csvLine = ",Alice,g1,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void rejectsEmptyGameId() {
        String csvLine = "p1,Alice,,Chess,10,85";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void trimsWhitespaceFromFields() {
        String csvLine = " p1 , Alice , g1 , Chess , 10 , 85 ";
        Result<ScoreRecord, ParseError> result = parser.parseLine(csvLine);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<ScoreRecord, ParseError> ok = (Result.Ok<ScoreRecord, ParseError>) result;
        ScoreRecord record = ok.value();

        assertThat(record.player().playerId()).isEqualTo("p1");
        assertThat(record.player().playerName()).isEqualTo("Alice");
        assertThat(record.gameEntry().gameId()).isEqualTo("g1");
        assertThat(record.gameEntry().gameName()).isEqualTo("Chess");
    }

    @Test
    void parsesMultipleValidLines() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,10,85",
            "p2,Bob,g2,Checkers,5,75"
        );
        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(csvLines);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<ScoreRecord>, ParseError> ok = (Result.Ok<List<ScoreRecord>, ParseError>) result;
        List<ScoreRecord> records = ok.value();

        assertThat(records).hasSize(2);
        assertThat(records.get(0).player().playerId()).isEqualTo("p1");
        assertThat(records.get(1).player().playerId()).isEqualTo("p2");
    }

    @Test
    void shortCircuitsOnFirstInvalidLine() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,10,85",
            "invalid",
            "p2,Bob,g2,Checkers,5,75"
        );
        Result<List<ScoreRecord>, ParseError> result = parser.parseLines(csvLines);

        assertThat(result).isInstanceOf(Result.Err.class);
    }
}
