package topthree.impl;

import topthree.interfaces.LeaderboardRanker;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {
    
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();
    
    @Test
    void rank_emptyInput_returnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());
        
        assertTrue(result.definiteWinners().isEmpty());
        assertTrue(result.tiedCandidates().isEmpty());
    }
}