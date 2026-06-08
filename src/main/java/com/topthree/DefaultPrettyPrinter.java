package com.topthree;

public class DefaultPrettyPrinter implements PrettyPrinter {

    @Override
    public String print(ScoreRecord record) {
        return String.join(",",
                record.player().playerId(),
                record.player().playerName(),
                record.gameEntry().gameId(),
                record.gameEntry().gameName(),
                String.valueOf(record.gameEntry().hoursPlayed()),
                String.valueOf(record.gameEntry().normalisedScore()));
    }
}
