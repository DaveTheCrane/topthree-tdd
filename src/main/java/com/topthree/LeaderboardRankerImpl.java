package com.topthree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of LeaderboardRanker for ranking players with tie-boundary handling.
 */
public class LeaderboardRankerImpl implements LeaderboardRanker {
    
    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates == null || aggregates.isEmpty()) {
            return new RankedResult(new ArrayList<>(), new ArrayList<>());
        }

        // Sort by total score descending
        List<PlayerAggregate> sorted = aggregates.stream()
            .sorted((a, b) -> Integer.compare(b.totalScore(), a.totalScore()))
            .collect(Collectors.toList());

        int n = sorted.size();

        // Case 1: Fewer than three players
        if (n < 3) {
            // Check if all players have the same score (only meaningful for n >= 2)
            boolean allSameScore = n > 1 && sorted.stream().allMatch(p -> p.totalScore() == sorted.get(0).totalScore());
            if (allSameScore) {
                // All share same score
                return new RankedResult(new ArrayList<>(), new ArrayList<>(sorted));
            }
            // No tie: all go to definite winners in order
            return new RankedResult(new ArrayList<>(sorted), new ArrayList<>());
        }

        // Case 2: Three or more players
        int boundaryScore = sorted.get(2).totalScore();
        
        // Check if there's a tie at position 3 (index 2)
        boolean hasTieAtBoundary = sorted.stream()
            .filter(p -> p.totalScore() == boundaryScore)
            .count() > 1;

        if (!hasTieAtBoundary) {
            // No tie: top 3 are definite winners
            return new RankedResult(sorted.subList(0, 3), new ArrayList<>());
        }

        // Tie at boundary: split players
        List<PlayerAggregate> definiteWinners = sorted.stream()
            .filter(p -> p.totalScore() > boundaryScore)
            .collect(Collectors.toList());

        List<PlayerAggregate> tiedCandidates = sorted.stream()
            .filter(p -> p.totalScore() == boundaryScore)
            .collect(Collectors.toList());

        return new RankedResult(definiteWinners, tiedCandidates);
    }
}
