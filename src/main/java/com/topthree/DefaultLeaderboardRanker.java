package com.topthree;

import com.topthree.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultLeaderboardRanker implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        // For now, place all in definiteWinners when <= 3
        if (sorted.size() <= 3) {
            return new RankedResult(sorted, List.of());
        }

        return new RankedResult(sorted.subList(0, 3), List.of());
    }
}
