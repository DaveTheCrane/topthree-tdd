package com.topthree;

import com.topthree.model.*;

import java.util.*;

public class DefaultScoreAggregator implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records.isEmpty()) {
            return Result.ok(List.of());
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
