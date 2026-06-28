package com.topthree.component;

import com.topthree.model.*;

import java.util.*;

public class ScoreAggregatorImpl implements ScoreAggregator {
    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        Map<String, PlayerAggregate> aggregates = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            String playerName = record.player().playerName();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();

            if (aggregates.containsKey(playerId)) {
                PlayerAggregate existing = aggregates.get(playerId);
                int newTotalScore = existing.totalScore() + weightedScore;
                PlayerAggregate updated = new PlayerAggregate(
                    new Player(playerId, playerName),
                    newTotalScore
                );
                aggregates.put(playerId, updated);
            } else {
                aggregates.put(playerId, new PlayerAggregate(
                    new Player(playerId, playerName),
                    weightedScore
                ));
            }
        }

        return Result.ok(new ArrayList<>(aggregates.values()));
    }
}
