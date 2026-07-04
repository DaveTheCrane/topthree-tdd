package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link ScoreAggregator} implementation.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        return Result.ok(List.of());
    }
}
