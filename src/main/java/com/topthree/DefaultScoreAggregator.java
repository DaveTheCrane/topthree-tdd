package com.topthree;

import com.topthree.model.*;

import java.util.*;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return Result.ok(List.of());
        }

        Map<String, Integer> scores = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            scores.merge(playerId, weightedScore, Integer::sum);
            names.put(playerId, record.player().playerName());
        }

        List<PlayerAggregate> result = new ArrayList<>();
        for (var entry : scores.entrySet()) {
            String playerId = entry.getKey();
            result.add(new PlayerAggregate(
                    new Player(playerId, names.get(playerId)),
                    entry.getValue()
            ));
        }
        return Result.ok(result);
    }
}
