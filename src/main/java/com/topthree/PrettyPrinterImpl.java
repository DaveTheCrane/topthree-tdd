package com.topthree;

/**
 * Implementation of PrettyPrinter interface.
 */
public class PrettyPrinterImpl implements PrettyPrinter {

    @Override
    public String print(ScoreRecord record) {
        Player player = record.player();
        GameEntry game = record.gameEntry();
        return String.format("%s,%s,%s,%s,%d,%d",
                player.playerId(),
                player.playerName(),
                game.gameId(),
                game.gameName(),
                game.hoursPlayed(),
                game.normalisedScore());
    }
}
