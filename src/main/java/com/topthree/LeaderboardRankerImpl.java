package com.topthree;

import java.util.List;

public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
