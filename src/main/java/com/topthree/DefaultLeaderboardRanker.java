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

        if (sorted.size() <= 3) {
            return new RankedResult(List.copyOf(sorted), List.of());
        }

        int boundaryScore = sorted.get(2).totalScore();
        if (sorted.get(3).totalScore() == boundaryScore) {
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
        } else {
            return new RankedResult(List.copyOf(sorted.subList(0, 3)), List.of());
        }
    }
}
