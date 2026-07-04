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
        
        java.util.Map<String, PlayerAggregate> aggregatesByPlayerId = new java.util.HashMap<>();
        
        for (ScoreRecord record : records) {
            int weightedScore = record.hoursPlayed() * record.normalizedScore();
            
            if (aggregatesByPlayerId.containsKey(record.playerId())) {
                PlayerAggregate existing = aggregatesByPlayerId.get(record.playerId());
                int newTotalScore = existing.totalScore() + weightedScore;
                PlayerAggregate updated = new PlayerAggregate(
                    existing.playerId(),
                    record.playerName(), // Use the latest player name
                    newTotalScore
                );
                aggregatesByPlayerId.put(record.playerId(), updated);
            } else {
                PlayerAggregate aggregate = new PlayerAggregate(
                    record.playerId(),
                    record.playerName(),
                    weightedScore
                );
                aggregatesByPlayerId.put(record.playerId(), aggregate);
            }
        }
        
        return new Result.Ok<>(new java.util.ArrayList<>(aggregatesByPlayerId.values()));
    }
}