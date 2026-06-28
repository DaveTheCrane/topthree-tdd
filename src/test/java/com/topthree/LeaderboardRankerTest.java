package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    // Feature: top-three-high-scores, Requirement 3.5: Empty input returns empty RankedResult
    @Test
    void rank_returnsEmptyResult_forEmptyInput() {
        RankedResult result = ranker.rank(List.of());
        
        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 3.4: Single player goes into definiteWinners
    @Test
    void rank_singlePlayer_inDefiniteWinners() {
        Player player = new Player("p1", "Alice");
        PlayerAggregate aggregate = new PlayerAggregate(player, 100);
        
        RankedResult result = ranker.rank(List.of(aggregate));
        
        assertEquals(List.of(aggregate), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 3.2: Three players with distinct scores - all in definiteWinners
    @Test
    void rank_threeDistinctScorePlayers_allInDefiniteWinners() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        Player p3 = new Player("p3", "Charlie");
        PlayerAggregate a1 = new PlayerAggregate(p1, 300);
        PlayerAggregate a2 = new PlayerAggregate(p2, 200);
        PlayerAggregate a3 = new PlayerAggregate(p3, 100);
        
        RankedResult result = ranker.rank(List.of(a1, a2, a3));
        
        assertEquals(List.of(a1, a2, a3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 3.3: Tie at position 3 - players above boundary in definiteWinners
    @Test
    void rank_tieAtPosition3_definiteWinnersAndTiedCandidates() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        Player p3 = new Player("p3", "Charlie");
        Player p4 = new Player("p4", "David");
        PlayerAggregate a1 = new PlayerAggregate(p1, 400);
        PlayerAggregate a2 = new PlayerAggregate(p2, 300);
        PlayerAggregate a3 = new PlayerAggregate(p3, 200);
        PlayerAggregate a4 = new PlayerAggregate(p4, 200);
        
        RankedResult result = ranker.rank(List.of(a1, a2, a3, a4));
        
        assertEquals(List.of(a1, a2), result.definiteWinners());
        assertEquals(List.of(a3, a4), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 3.6: All players share same score - definiteWinners empty
    @Test
    void rank_allTied_definiteWinnersEmpty() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        Player p3 = new Player("p3", "Charlie");
        PlayerAggregate a1 = new PlayerAggregate(p1, 100);
        PlayerAggregate a2 = new PlayerAggregate(p2, 100);
        PlayerAggregate a3 = new PlayerAggregate(p3, 100);
        
        RankedResult result = ranker.rank(List.of(a1, a2, a3));
        
        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(a1, a2, a3), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 3.4: Two distinct score players both in definiteWinners
    @Test
    void rank_twoDistinctScorePlayers_bothInDefiniteWinners() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        PlayerAggregate a1 = new PlayerAggregate(p1, 200);
        PlayerAggregate a2 = new PlayerAggregate(p2, 100);
        
        RankedResult result = ranker.rank(List.of(a1, a2));
        
        assertEquals(List.of(a1, a2), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }
}
