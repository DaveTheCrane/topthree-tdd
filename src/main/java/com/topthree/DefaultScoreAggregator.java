package com.topthree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        Map<String, Player> playerById = new LinkedHashMap<>();
        Map<String, Integer> scoreById = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();

            playerById.put(playerId, record.player());
            scoreById.merge(playerId, weightedScore, Integer::sum);
        }

        List<PlayerAggregate> aggregates = new ArrayList<>();
        for (Map.Entry<String, Player> entry : playerById.entrySet()) {
            String playerId = entry.getKey();
            Player player = entry.getValue();
            int totalScore = scoreById.get(playerId);
            aggregates.add(new PlayerAggregate(player, totalScore));
        }

        return new Result.Ok<>(aggregates);
    }
}
