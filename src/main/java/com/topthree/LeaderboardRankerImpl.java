package com.topthree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of LeaderboardRanker interface.
 */
public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }
        
        // Sort descending by totalScore
        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt((PlayerAggregate a) -> a.totalScore()).reversed());
        
        int n = sorted.size();
        
        // Check if all players have the same score (degenerate all-tied case)
        // This should be checked first per Requirement 3.6
        // All-tied means 2 or more players all share the same score
        if (n >= 2) {
            int firstScore = sorted.get(0).totalScore();
            boolean allSameScore = sorted.stream().allMatch(a -> a.totalScore() == firstScore);
            
            if (allSameScore) {
                return new RankedResult(List.of(), sorted);
            }
        }
        
        // If fewer than 3 players, all go to definiteWinners
        if (n <= 3) {
            return new RankedResult(sorted, List.of());
        }
        
        // Find the boundary score (score at position 2 - the 3rd place)
        int boundaryScore = sorted.get(2).totalScore();
        
        // Check if player at position 3 has same score as boundary
        if (sorted.get(3).totalScore() == boundaryScore) {
            // Tie at boundary - find all players with boundary score
            List<PlayerAggregate> definiteWinners = new ArrayList<>();
            List<PlayerAggregate> tiedCandidates = new ArrayList<>();
            
            for (PlayerAggregate aggregate : sorted) {
                if (aggregate.totalScore() > boundaryScore) {
                    definiteWinners.add(aggregate);
                } else {
                    tiedCandidates.add(aggregate);
                }
            }
            
            return new RankedResult(definiteWinners, tiedCandidates);
        } else {
            // No tie at boundary - top 3 in definiteWinners
            return new RankedResult(sorted.subList(0, 3), List.of());
        }
    }
}
