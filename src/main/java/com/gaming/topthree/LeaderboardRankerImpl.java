package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link LeaderboardRanker} implementation.
 */
public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        return new RankedResult(List.of(), List.of());
    }
}
