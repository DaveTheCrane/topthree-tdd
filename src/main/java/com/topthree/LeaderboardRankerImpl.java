package com.topthree;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        List<PlayerAggregate> topThree = sorted.subList(0, Math.min(3, sorted.size()));

        return new RankedResult(List.copyOf(topThree), List.of());
    }
}
