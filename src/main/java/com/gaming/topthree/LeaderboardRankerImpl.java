package com.gaming.topthree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Default {@link LeaderboardRanker} implementation.
 */
public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());
        return new RankedResult(sorted, List.of());
    }
}
