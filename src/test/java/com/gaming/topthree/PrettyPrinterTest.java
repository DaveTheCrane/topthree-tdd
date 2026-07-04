package com.gaming.topthree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrettyPrinterTest {

    private final PrettyPrinter printer = new PrettyPrinterImpl();

    @Test
    void formatsScoreRecordAsCommaSeparatedString() {
        ScoreRecord record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 10, 85)
        );

        String csv = printer.print(record);

        assertThat(csv).isEqualTo("p1,Alice,g1,Chess,10,85");
    }
}
