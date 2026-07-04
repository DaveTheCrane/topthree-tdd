package topthree.impl;

import topthree.interfaces.ScoreAggregator;
import topthree.models.AggregationError;
import topthree.models.PlayerAggregate;
import topthree.models.Result;
import topthree.models.ScoreRecord;
import java.util.List;

public class ScoreAggregatorImpl implements ScoreAggregator {
    
    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return new Result.Ok<>(List.of());
        }
        
        // For now, handle single record
        ScoreRecord record = records.get(0);
        int totalScore = record.hoursPlayed() * record.normalizedScore();
        PlayerAggregate aggregate = new PlayerAggregate(
            record.playerId(),
            record.playerName(),
            totalScore
        );
        
        return new Result.Ok<>(List.of(aggregate));
    }
}