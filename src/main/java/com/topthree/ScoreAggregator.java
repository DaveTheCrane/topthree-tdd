package com.topthree;

import java.util.List;

/**
 * Converts a list of Score_Record objects into one Player_Aggregate per distinct player id.
 */
public interface ScoreAggregator {

    /**
     * Aggregates score records by player id.
     * If the same player id appears with different display names, the last-seen name wins.
     * Never returns an error for name conflicts.
     */
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}
