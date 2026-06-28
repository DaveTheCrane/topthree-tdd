package com.topthree.component;

import com.topthree.model.GameEntry;
import com.topthree.model.Player;
import com.topthree.model.ScoreRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PrettyPrinterTest {
    private PrettyPrinter printer;

    @BeforeEach
    void setup() {
        printer = new PrettyPrinterImpl();
    }

    @Test
    void formatsScoreRecordAsCsv() {
        Player player = new Player("p1", "Alice");
        GameEntry gameEntry = new GameEntry("g1", "Chess", 10, 85);
        ScoreRecord record = new ScoreRecord(player, gameEntry);

        String csv = printer.print(record);

        assertThat(csv).isEqualTo("p1,Alice,g1,Chess,10,85");
    }
}
