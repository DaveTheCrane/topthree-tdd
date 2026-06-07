package com.topthree;

import java.util.ArrayList;
import java.util.List;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return new Result.Ok<>(List.of());
        }
        List<PlayerAggregate> result = new ArrayList<>();
        for (ScoreRecord record : records) {
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            result.add(new PlayerAggregate(record.player(), weightedScore));
        }
        return new Result.Ok<>(result);
    }
}
