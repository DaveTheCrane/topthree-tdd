package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link ScoreAggregator} implementation.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return Result.ok(List.of());
        }

        ScoreRecord record = records.get(0);
        int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
        PlayerAggregate aggregate = new PlayerAggregate(record.player(), weightedScore);
        return Result.ok(List.of(aggregate));
    }
}
