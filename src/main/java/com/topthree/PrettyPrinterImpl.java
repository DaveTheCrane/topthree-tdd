package com.topthree;

public class PrettyPrinterImpl implements PrettyPrinter {

    @Override
    public String print(ScoreRecord record) {
        Player player = record.player();
        GameEntry game = record.gameEntry();
        return player.playerId() + "," +
                player.playerName() + "," +
                game.gameId() + "," +
                game.gameName() + "," +
                game.hoursPlayed() + "," +
                game.normalisedScore();
    }
}
