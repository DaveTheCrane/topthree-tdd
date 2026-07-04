package topthree.impl;

import topthree.interfaces.PrettyPrinter;
import topthree.models.ScoreRecord;

public class PrettyPrinterImpl implements PrettyPrinter {
    
    @Override
    public String print(ScoreRecord record) {
        return String.join(",",
            record.playerId(),
            record.playerName(),
            record.gameId(),
            record.gameName(),
            String.valueOf(record.hoursPlayed()),
            String.valueOf(record.normalizedScore())
        );
    }
}