package com.topthree;

public class PrettyPrinterImpl implements PrettyPrinter {

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
