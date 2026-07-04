package topthree.impl;

import topthree.interfaces.LeaderboardRanker;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;
import java.util.List;

public class LeaderboardRankerImpl implements LeaderboardRanker {
    
    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }
        
        // Sort in descending order by totalScore
        List<PlayerAggregate> sorted = new java.util.ArrayList<>(aggregates);
        sorted.sort((a, b) -> Integer.compare(b.totalScore(), a.totalScore()));
        
        // For ≤3 players, all go in definiteWinners
        if (sorted.size() <= 3) {
            return new RankedResult(sorted, List.of());
        }
        
        // Check for tie at position 3 (index 2)
        int boundaryScore = sorted.get(2).totalScore();
        
        // Check if there's a tie at the boundary
        // Count how many players have score == boundaryScore
        int tieCount = 0;
        for (PlayerAggregate player : sorted) {
            if (player.totalScore() == boundaryScore) {
                tieCount++;
            }
        }
        
        List<PlayerAggregate> definiteWinners = new java.util.ArrayList<>();
        List<PlayerAggregate> tiedCandidates = new java.util.ArrayList<>();
        
        if (tieCount == 1) {
            // No tie at boundary - take top 3 as definite winners
            definiteWinners = sorted.subList(0, 3);
        } else {
            // Tie at boundary - split between definiteWinners and tiedCandidates
            for (PlayerAggregate player : sorted) {
                if (player.totalScore() > boundaryScore) {
                    definiteWinners.add(player);
                } else if (player.totalScore() == boundaryScore) {
                    tiedCandidates.add(player);
                } else {
                    break; // Scores are sorted, so we can stop
                }
            }
        }
        
        return new RankedResult(definiteWinners, tiedCandidates);
    }
}