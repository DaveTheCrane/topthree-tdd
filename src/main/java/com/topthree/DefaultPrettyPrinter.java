package com.topthree;

import com.topthree.model.ScoreRecord;

public class DefaultPrettyPrinter implements PrettyPrinter {

    @Override
    public String print(ScoreRecord record) {
        return record.player().playerId() + ","
                + record.player().playerName() + ","
                + record.gameEntry().gameId() + ","
                + record.gameEntry().gameName() + ","
                + record.gameEntry().hoursPlayed() + ","
                + record.gameEntry().normalisedScore();
    }
}
