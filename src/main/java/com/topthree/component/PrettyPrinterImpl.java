package com.topthree.component;

import com.topthree.model.ScoreRecord;

public class PrettyPrinterImpl implements PrettyPrinter {
    @Override
    public String print(ScoreRecord record) {
        return String.format("%s,%s,%s,%s,%d,%d",
            record.player().playerId(),
            record.player().playerName(),
            record.gameEntry().gameId(),
            record.gameEntry().gameName(),
            record.gameEntry().hoursPlayed(),
            record.gameEntry().normalisedScore()
        );
    }
}
