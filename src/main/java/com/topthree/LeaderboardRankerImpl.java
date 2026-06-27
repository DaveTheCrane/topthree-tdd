package com.topthree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());

        List<PlayerAggregate> definiteWinners = sorted.subList(0, Math.min(3, sorted.size()));
        return new RankedResult(List.copyOf(definiteWinners), List.of());
    }
}
