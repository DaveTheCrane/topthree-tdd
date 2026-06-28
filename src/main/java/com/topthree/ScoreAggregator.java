package com.topthree;

import java.util.List;

/**
 * Aggregates ScoreRecord objects by player id to produce PlayerAggregate objects.
 * 
 * For each player, computes:
 * - Total weighted score: sum of (hours_played × normalised_score) across all their game entries
 * - Player aggregate: one PlayerAggregate per distinct player id
 * 
 * If the same player id appears with different display names, the last-seen name wins.
 */
public interface ScoreAggregator {
    /**
     * Aggregates a list of ScoreRecords by player id.
     * 
     * @param records the list of ScoreRecords to aggregate
     * @return Ok(List<PlayerAggregate>) containing one aggregate per distinct player id,
     *         or Err(AggregationError) if an error occurs
     */
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}
