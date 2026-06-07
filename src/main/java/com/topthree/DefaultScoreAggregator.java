package com.topthree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return new Result.Ok<>(List.of());
        }
        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        Map<String, Player> playerMap = new LinkedHashMap<>();
        for (ScoreRecord record : records) {
            String pid = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            scoreMap.merge(pid, weightedScore, Integer::sum);
            playerMap.put(pid, record.player());
        }
        List<PlayerAggregate> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            result.add(new PlayerAggregate(playerMap.get(entry.getKey()), entry.getValue()));
        }
        return new Result.Ok<>(result);
    }
}
