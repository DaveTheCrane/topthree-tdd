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
        
        // Take top 3 for definiteWinners (no tie handling yet)
        List<PlayerAggregate> definiteWinners = sorted.subList(0, 3);
        return new RankedResult(definiteWinners, List.of());
    }
}