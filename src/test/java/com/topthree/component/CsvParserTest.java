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
}
