package com.topthree;

import java.util.List;

public interface ScoreAggregator {
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}
