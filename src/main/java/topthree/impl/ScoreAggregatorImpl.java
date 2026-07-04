package topthree.impl;

import topthree.interfaces.ScoreAggregator;
import topthree.models.AggregationError;
import topthree.models.PlayerAggregate;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static topthree.models.Result.Err;
import static topthree.models.Result.Ok;

public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return new Ok<>(List.of());
        }

        Map<String, PlayerAggregate> aggregates = new HashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.playerId();
            int weightedScore = record.hoursPlayed() * record.normalisedScore();

            if (aggregates.containsKey(playerId)) {
                PlayerAggregate existing = aggregates.get(playerId);
                int newTotal = existing.totalScore() + weightedScore;
                aggregates.put(playerId, new PlayerAggregate(playerId, record.playerName(), newTotal));
            } else {
                aggregates.put(playerId, new PlayerAggregate(playerId, record.playerName(), weightedScore));
            }
        }

        return new Ok<>(new ArrayList<>(aggregates.values()));
    }
}
