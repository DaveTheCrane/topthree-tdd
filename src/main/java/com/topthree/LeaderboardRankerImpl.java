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

        int n = sorted.size();

        // Degenerate case: all players share the same score
        boolean allSame = sorted.stream()
                .allMatch(p -> p.totalScore() == sorted.get(0).totalScore());
        if (allSame && n > 1) {
            return new RankedResult(List.of(), List.copyOf(sorted));
        }

        if (n <= 3) {
            // No tie (allSame already handled above for n > 1), so all go to definiteWinners
            return new RankedResult(List.copyOf(sorted), List.of());
        }

        // N > 3
        int boundaryScore = sorted.get(2).totalScore();
        if (sorted.get(3).totalScore() == boundaryScore) {
            // Tie at boundary
            List<PlayerAggregate> definiteWinners = sorted.stream()
                    .filter(p -> p.totalScore() > boundaryScore)
                    .collect(Collectors.toList());
            List<PlayerAggregate> tiedCandidates = sorted.stream()
                    .filter(p -> p.totalScore() == boundaryScore)
                    .collect(Collectors.toList());
            return new RankedResult(List.copyOf(definiteWinners), List.copyOf(tiedCandidates));
        } else {
            // No tie at boundary — top three are definite winners
            List<PlayerAggregate> topThree = sorted.subList(0, 3);
            return new RankedResult(List.copyOf(topThree), List.of());
        }
    }
}
