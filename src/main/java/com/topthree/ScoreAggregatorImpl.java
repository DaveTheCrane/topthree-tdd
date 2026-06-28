package com.topthree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ScoreAggregator interface.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        Map<String, PlayerAggregateData> dataMap = new HashMap<>();
        
        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            String playerName = record.player().playerName();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            
            if (dataMap.containsKey(playerId)) {
                PlayerAggregateData data = dataMap.get(playerId);
                data.totalScore += weightedScore;
                data.playerName = playerName;
            } else {
                PlayerAggregateData data = new PlayerAggregateData();
                data.playerId = playerId;
                data.playerName = playerName;
                data.totalScore = weightedScore;
                dataMap.put(playerId, data);
            }
        }
        
        List<PlayerAggregate> aggregates = new ArrayList<>();
        for (PlayerAggregateData data : dataMap.values()) {
            Player player = new Player(data.playerId, data.playerName);
            aggregates.add(new PlayerAggregate(player, data.totalScore));
        }
        
        return Result.ok(aggregates);
    }

    private static class PlayerAggregateData {
        String playerId;
        String playerName;
        int totalScore;
    }
}
