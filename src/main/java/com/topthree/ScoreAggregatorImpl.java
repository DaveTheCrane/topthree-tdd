package com.topthree;

import java.util.List;

public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        return new Result.Err<>(new AggregationError("Not implemented", ""));
    }
}
