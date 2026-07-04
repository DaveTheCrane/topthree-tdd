package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private final CsvParser parser = new DefaultCsvParser();

    @Test
    void parsesValidSixFieldCsvLine() {
        var result = parser.parseLine("p1,Alice,g1,Chess,10,85");

        assertInstanceOf(Result.Ok.class, result);
        var record = ((Result.Ok<ScoreRecord, ParseError>) result).value();
        assertEquals("p1", record.player().playerId());
        assertEquals("Alice", record.player().playerName());
        assertEquals("g1", record.gameEntry().gameId());
        assertEquals("Chess", record.gameEntry().gameName());
        assertEquals(10, record.gameEntry().hoursPlayed());
        assertEquals(85, record.gameEntry().normalisedScore());
    }
}
