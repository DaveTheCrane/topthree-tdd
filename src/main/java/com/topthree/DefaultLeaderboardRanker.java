package com.topthree;

import java.util.Comparator;
import java.util.List;
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

        // Check if all players share the same score (degenerate tie case, only applies with 2+ players)
        int firstScore = sorted.get(0).totalScore();
        int lastScore = sorted.get(n - 1).totalScore();
        if (n > 1 && firstScore == lastScore) {
            // All players tied — definiteWinners empty, all in tiedCandidates
            return new RankedResult(List.of(), List.copyOf(sorted));
        }

        if (n <= 3) {
            // N <= 3 and not all tied (handled above) → all in definiteWinners
            return new RankedResult(List.copyOf(sorted), List.of());
        }

        // N > 3
        int boundaryScore = sorted.get(2).totalScore();

        if (sorted.get(3).totalScore() == boundaryScore) {
            // Tie at boundary — partition
            List<PlayerAggregate> definiteWinners = sorted.stream()
                    .filter(p -> p.totalScore() > boundaryScore)
                    .collect(Collectors.toList());
            List<PlayerAggregate> tiedCandidates = sorted.stream()
                    .filter(p -> p.totalScore() == boundaryScore)
                    .collect(Collectors.toList());
            return new RankedResult(definiteWinners, tiedCandidates);
        } else {
            // No tie at boundary — top 3 are definite winners
            List<PlayerAggregate> definiteWinners = sorted.stream()
                    .limit(3)
                    .collect(Collectors.toList());
            return new RankedResult(definiteWinners, List.of());
        }
    }
}
