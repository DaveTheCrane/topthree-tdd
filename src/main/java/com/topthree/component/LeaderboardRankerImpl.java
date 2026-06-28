package com.topthree.component;

import com.topthree.model.PlayerAggregate;
import com.topthree.model.RankedResult;

import java.util.*;

public class LeaderboardRankerImpl implements LeaderboardRanker {
    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        // Sort descending by totalScore
        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort((a, b) -> Integer.compare(b.totalScore(), a.totalScore()));

        // Take top 3
        int topCount = Math.min(3, sorted.size());
        List<PlayerAggregate> topThree = sorted.subList(0, topCount);

        // Check for tie at boundary
        if (topCount < 3) {
            // Fewer than 3 players, all go to definiteWinners
            return new RankedResult(topThree, List.of());
        }

        // topCount == 3, check if there's a tie at position 3 (index 2)
        int boundaryScore = topThree.get(2).totalScore();

        // Check if position 1 and 2 have the same score (all tied)
        if (topThree.get(0).totalScore() == boundaryScore) {
            // All three are tied, all go to tiedCandidates
            return new RankedResult(List.of(), topThree);
        }

        // Check if there are more players with the same boundary score
        boolean tieAtBoundary = sorted.stream()
            .skip(3)
            .anyMatch(p -> p.totalScore() == boundaryScore);

        if (!tieAtBoundary) {
            // No tie at boundary
            return new RankedResult(topThree, List.of());
        }

        // Tie at boundary - split into definiteWinners and tiedCandidates
        List<PlayerAggregate> definiteWinners = new ArrayList<>();
        List<PlayerAggregate> tiedCandidates = new ArrayList<>();

        for (PlayerAggregate p : sorted) {
            if (p.totalScore() > boundaryScore) {
                definiteWinners.add(p);
            } else if (p.totalScore() == boundaryScore) {
                tiedCandidates.add(p);
            }
        }

        return new RankedResult(definiteWinners, tiedCandidates);
    }
}
