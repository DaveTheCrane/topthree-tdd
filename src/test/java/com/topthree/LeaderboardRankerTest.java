package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    @Test
    void threeDistinctScoresAllInDefiniteWinners() {
        var agg1 = new PlayerAggregate(new Player("p1", "Alice"), 300);
        var agg2 = new PlayerAggregate(new Player("p2", "Bob"), 200);
        var agg3 = new PlayerAggregate(new Player("p3", "Carol"), 100);
        var result = ranker.rank(List.of(agg3, agg1, agg2));

        assertEquals(3, result.definiteWinners().size());
        assertEquals(300, result.definiteWinners().get(0).totalScore());
        assertEquals(200, result.definiteWinners().get(1).totalScore());
        assertEquals(100, result.definiteWinners().get(2).totalScore());
        assertTrue(result.tiedCandidates().isEmpty());
    }

    @Test
    void singlePlayerGoesInDefiniteWinners() {
        var agg = new PlayerAggregate(new Player("p1", "Alice"), 100);
        var result = ranker.rank(List.of(agg));

        assertEquals(1, result.definiteWinners().size());
        assertEquals(agg, result.definiteWinners().get(0));
        assertTrue(result.tiedCandidates().isEmpty());
    }

    @Test
    void twoPlayersGoInDefiniteWinnersDescending() {
        var agg1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        var agg2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        var result = ranker.rank(List.of(agg2, agg1)); // pass in non-sorted order

        assertEquals(2, result.definiteWinners().size());
        assertEquals(200, result.definiteWinners().get(0).totalScore());
        assertEquals(100, result.definiteWinners().get(1).totalScore());
        assertTrue(result.tiedCandidates().isEmpty());
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = ranker.rank(List.of());

        assertTrue(result.definiteWinners().isEmpty());
        assertTrue(result.tiedCandidates().isEmpty());
    }
}
