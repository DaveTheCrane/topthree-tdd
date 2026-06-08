package com.topthree;

import java.util.ArrayList;
import java.util.List;

public class DefaultLeaderboardRanker implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        return new RankedResult(new ArrayList<>(aggregates), List.of());
    }
}
