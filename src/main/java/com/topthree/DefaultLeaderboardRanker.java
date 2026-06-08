package com.topthree;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultLeaderboardRanker implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        List<PlayerAggregate> definiteWinners = sorted.stream()
                .limit(3)
                .collect(Collectors.toList());

        return new RankedResult(definiteWinners, List.of());
    }
}
