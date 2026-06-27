package com.topthree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        Map<String, Player> playerById = new LinkedHashMap<>();
        Map<String, Integer> scoreById = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String id = record.player().playerId();
            playerById.put(id, record.player());
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            scoreById.merge(id, weightedScore, Integer::sum);
        }

        List<PlayerAggregate> aggregates = new ArrayList<>();
        for (Map.Entry<String, Player> entry : playerById.entrySet()) {
            String id = entry.getKey();
            aggregates.add(new PlayerAggregate(entry.getValue(), scoreById.get(id)));
        }

        return new Result.Ok<>(aggregates);
    }
}
