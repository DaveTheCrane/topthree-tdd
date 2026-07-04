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
    
    @Test
    void rank_singlePlayer_goesInDefiniteWinners() {
        PlayerAggregate player = new PlayerAggregate("p1", "Alice", 100);
        RankedResult result = ranker.rank(List.of(player));
        
        assertEquals(1, result.definiteWinners().size());
        assertTrue(result.tiedCandidates().isEmpty());
        assertEquals("p1", result.definiteWinners().get(0).playerId());
        assertEquals(100, result.definiteWinners().get(0).totalScore());
    }
    
    @Test
    void rank_twoPlayers_bothInDefiniteWinnersDescendingOrder() {
        PlayerAggregate player1 = new PlayerAggregate("p1", "Alice", 200);
        PlayerAggregate player2 = new PlayerAggregate("p2", "Bob", 100);
        RankedResult result = ranker.rank(List.of(player1, player2));
        
        assertEquals(2, result.definiteWinners().size());
        assertTrue(result.tiedCandidates().isEmpty());
        
        // Should be in descending order
        assertEquals("p1", result.definiteWinners().get(0).playerId());
        assertEquals(200, result.definiteWinners().get(0).totalScore());
        assertEquals("p2", result.definiteWinners().get(1).playerId());
        assertEquals(100, result.definiteWinners().get(1).totalScore());
    }
    
    @Test
    void rank_threeDistinctScores_allInDefiniteWinnersDescending() {
        PlayerAggregate player1 = new PlayerAggregate("p1", "Alice", 300);
        PlayerAggregate player2 = new PlayerAggregate("p2", "Bob", 200);
        PlayerAggregate player3 = new PlayerAggregate("p3", "Charlie", 100);
        RankedResult result = ranker.rank(List.of(player1, player2, player3));
        
        assertEquals(3, result.definiteWinners().size());
        assertTrue(result.tiedCandidates().isEmpty());
        
        // Should be in descending order
        assertEquals("p1", result.definiteWinners().get(0).playerId());
        assertEquals(300, result.definiteWinners().get(0).totalScore());
        assertEquals("p2", result.definiteWinners().get(1).playerId());
        assertEquals(200, result.definiteWinners().get(1).totalScore());
        assertEquals("p3", result.definiteWinners().get(2).playerId());
        assertEquals(100, result.definiteWinners().get(2).totalScore());
    }
    
    @Test
    void rank_fourPlayersNoTie_topThreeInDefiniteWinners() {
        PlayerAggregate player1 = new PlayerAggregate("p1", "Alice", 400);
        PlayerAggregate player2 = new PlayerAggregate("p2", "Bob", 300);
        PlayerAggregate player3 = new PlayerAggregate("p3", "Charlie", 200);
        PlayerAggregate player4 = new PlayerAggregate("p4", "David", 100);
        RankedResult result = ranker.rank(List.of(player1, player2, player3, player4));
        
        assertEquals(3, result.definiteWinners().size());
        assertTrue(result.tiedCandidates().isEmpty());
        
        // Top 3 should be in definiteWinners
        assertEquals("p1", result.definiteWinners().get(0).playerId());
        assertEquals(400, result.definiteWinners().get(0).totalScore());
        assertEquals("p2", result.definiteWinners().get(1).playerId());
        assertEquals(300, result.definiteWinners().get(1).totalScore());
        assertEquals("p3", result.definiteWinners().get(2).playerId());
        assertEquals(200, result.definiteWinners().get(2).totalScore());
    }
}