package com.gaming.topthree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Default {@link LeaderboardRanker} implementation.
 */
public class LeaderboardRankerImpl implements LeaderboardRanker {

    private static final int TOP_N = 3;

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());

        if (sorted.size() <= TOP_N) {
            return new RankedResult(sorted, List.of());
        }

        // More than three players: the boundary score is the third-place score.
        int boundaryScore = sorted.get(TOP_N - 1).totalScore();
        boolean tieAtBoundary = sorted.get(TOP_N).totalScore() == boundaryScore;

        if (!tieAtBoundary) {
            return new RankedResult(new ArrayList<>(sorted.subList(0, TOP_N)), List.of());
        }

        List<PlayerAggregate> definiteWinners = new ArrayList<>();
        List<PlayerAggregate> tiedCandidates = new ArrayList<>();
        for (PlayerAggregate aggregate : sorted) {
            if (aggregate.totalScore() > boundaryScore) {
                definiteWinners.add(aggregate);
            } else if (aggregate.totalScore() == boundaryScore) {
                tiedCandidates.add(aggregate);
            }
        }
        return new RankedResult(definiteWinners, tiedCandidates);
    }
}
