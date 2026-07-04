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

        int n = sorted.size();

        // Check if all players share the same score
        int highestScore = sorted.get(0).totalScore();
        boolean allSame = sorted.stream().allMatch(a -> a.totalScore() == highestScore);
        if (allSame && n >= 3) {
            return new RankedResult(List.of(), sorted);
        }

        // If 3 or fewer with distinct scores (or fewer than 3 all-same), all are definite winners
        if (n <= 3) {
            return new RankedResult(sorted, List.of());
        }

        // More than 3 players: check for tie at boundary
        int boundaryScore = sorted.get(2).totalScore();
        if (n > 3 && sorted.get(3).totalScore() == boundaryScore) {
            // Tie at boundary
            List<PlayerAggregate> winners = sorted.stream()
                    .filter(a -> a.totalScore() > boundaryScore)
                    .collect(Collectors.toList());
            List<PlayerAggregate> tied = sorted.stream()
                    .filter(a -> a.totalScore() == boundaryScore)
                    .collect(Collectors.toList());
            return new RankedResult(winners, tied);
        }

        // No tie at boundary: top 3
        return new RankedResult(sorted.subList(0, 3), List.of());
    }
}
