package com.topthree.component;

import com.topthree.model.AggregationError;
import com.topthree.model.PlayerAggregate;
import com.topthree.model.Result;
import com.topthree.model.ScoreRecord;

import java.util.List;

public interface ScoreAggregator {
    /**
     * Aggregates score records by player id.
     * If the same player id appears with different display names, the last-seen name wins.
     * Never returns an error for name conflicts.
     */
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}
