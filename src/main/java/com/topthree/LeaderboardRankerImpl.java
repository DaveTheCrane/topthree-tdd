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

        // Check if all players share the same score (requires 2+ players for a tie)
        int highestScore = sorted.get(0).totalScore();
        boolean allSameScore = sorted.size() >= 2 && sorted.stream().allMatch(p -> p.totalScore() == highestScore);
        if (allSameScore) {
            return new RankedResult(List.of(), List.copyOf(sorted));
        }

        // If 3 or fewer players, no boundary tie possible with distinct scores
        if (sorted.size() <= 3) {
            return new RankedResult(List.copyOf(sorted), List.of());
        }

        // Check for tie at boundary (position index 2)
        int boundaryScore = sorted.get(2).totalScore();
        if (sorted.size() > 3 && sorted.get(3).totalScore() == boundaryScore) {
            // Tie at boundary: split into definiteWinners (above boundary) and tiedCandidates (at boundary)
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

        // No tie at boundary: top 3 are definite winners
        List<PlayerAggregate> definiteWinners = sorted.subList(0, 3);
        return new RankedResult(List.copyOf(definiteWinners), List.of());
    }
}
