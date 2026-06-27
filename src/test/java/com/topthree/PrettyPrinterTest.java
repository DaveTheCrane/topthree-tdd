package com.topthree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrettyPrinterTest {

    private final PrettyPrinter prettyPrinter = new PrettyPrinterImpl();

    @Test
    void printFormatsScoreRecordAsSixFieldCsv() {
        ScoreRecord record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 10, 85)
        );

        String result = prettyPrinter.print(record);

        assertEquals("p1,Alice,g1,Chess,10,85", result);
    }
}
