package com.topthree;

/**
 * Implementation of PrettyPrinter for formatting ScoreRecords as CSV strings.
 */
public class PrettyPrinterImpl implements PrettyPrinter {
    
    @Override
    public String print(ScoreRecord record) {
        Player player = record.player();
        GameEntry gameEntry = record.gameEntry();
        
        return String.format("%s,%s,%s,%s,%d,%d",
            player.playerId(),
            player.playerName(),
            gameEntry.gameId(),
            gameEntry.gameName(),
            gameEntry.hoursPlayed(),
            gameEntry.normalisedScore()
        );
    }
}
