package com.gaming.topthree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ScoreAggregator} implementation.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        Map<String, Integer> totalsByPlayerId = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            totalsByPlayerId.merge(playerId, weightedScore, Integer::sum);
        }

        List<PlayerAggregate> aggregates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totalsByPlayerId.entrySet()) {
            Player player = firstPlayerWithId(records, entry.getKey());
            aggregates.add(new PlayerAggregate(player, entry.getValue()));
        }
        return Result.ok(aggregates);
    }

    private static Player firstPlayerWithId(List<ScoreRecord> records, String playerId) {
        for (ScoreRecord record : records) {
            if (record.player().playerId().equals(playerId)) {
                return record.player();
            }
        }
        throw new IllegalStateException("player id not found: " + playerId);
    }
}
