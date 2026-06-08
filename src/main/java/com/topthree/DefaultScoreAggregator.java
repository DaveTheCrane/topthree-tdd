package com.topthree;

import java.util.List;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
