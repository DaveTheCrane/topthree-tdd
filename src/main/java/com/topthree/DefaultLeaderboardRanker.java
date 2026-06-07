package com.topthree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DefaultLeaderboardRanker implements LeaderboardRanker {
    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());

        int n = sorted.size();
        if (n == 1) {
            return new RankedResult(List.copyOf(sorted), List.of());
        }

        int boundaryScore = sorted.get(Math.min(2, n - 1)).totalScore();
        boolean tieAtBoundary = sorted.stream()
                .filter(p -> p.totalScore() == boundaryScore)
                .count() > 1;

        if (!tieAtBoundary) {
            return new RankedResult(List.copyOf(sorted.subList(0, Math.min(3, n))), List.of());
        }

        List<PlayerAggregate> definiteWinners = new ArrayList<>();
        List<PlayerAggregate> tiedCandidates = new ArrayList<>();
        for (PlayerAggregate p : sorted) {
            if (p.totalScore() > boundaryScore) {
                definiteWinners.add(p);
            } else if (p.totalScore() == boundaryScore) {
                tiedCandidates.add(p);
            }
        }
        return new RankedResult(List.copyOf(definiteWinners), List.copyOf(tiedCandidates));
    }
}
