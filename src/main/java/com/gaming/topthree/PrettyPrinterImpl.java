package com.gaming.topthree;

/**
 * Default {@link PrettyPrinter} implementation.
 */
public class PrettyPrinterImpl implements PrettyPrinter {

    @Override
    public String print(ScoreRecord record) {
        Player player = record.player();
        GameEntry gameEntry = record.gameEntry();
        return String.join(",",
                player.playerId(),
                player.playerName(),
                gameEntry.gameId(),
                gameEntry.gameName(),
                String.valueOf(gameEntry.hoursPlayed()),
                String.valueOf(gameEntry.normalisedScore()));
    }
}
