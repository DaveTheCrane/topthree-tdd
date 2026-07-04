package com.gaming.topthree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ScoreAggregator} implementation.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {

    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        // Single pass: accumulate the running total and the last-seen player per player id.
        // LinkedHashMap preserves first-appearance ordering of player ids.
        Map<String, Accumulator> byPlayerId = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            byPlayerId.computeIfAbsent(playerId, id -> new Accumulator())
                    .add(record.player(), weightedScore);
        }

        List<PlayerAggregate> aggregates = new ArrayList<>();
        for (Accumulator acc : byPlayerId.values()) {
            aggregates.add(new PlayerAggregate(acc.lastPlayer, acc.totalScore));
        }
        return Result.ok(aggregates);
    }

    /** Mutable per-player accumulator used during aggregation. */
    private static final class Accumulator {
        private Player lastPlayer;
        private int totalScore;

        void add(Player player, int weightedScore) {
            this.lastPlayer = player;   // last-seen display name wins
            this.totalScore += weightedScore;
        }
    }
}
