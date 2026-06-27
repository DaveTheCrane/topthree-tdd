package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = ranker.rank(List.of());

        assertEquals(new RankedResult(List.of(), List.of()), result);
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinnersWithEmptyTiedCandidates() {
        var agg = new PlayerAggregate(new Player("p1", "Alice"), 100);

        var result = ranker.rank(List.of(agg));

        assertEquals(new RankedResult(List.of(agg), List.of()), result);
    }

    @Test
    void threeDistinctScoresAllInDefiniteWinnersDescending() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 300);

        var result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(new RankedResult(List.of(p3, p1, p2), List.of()), result);
    }

    @Test
    void fourPlayersNoTieTopThreeInDefiniteWinnersFourthExcluded() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        var p4 = new PlayerAggregate(new Player("p4", "Diana"), 100);

        var result = ranker.rank(List.of(p4, p2, p1, p3));

        assertEquals(new RankedResult(List.of(p1, p2, p3), List.of()), result);
    }

    @Test
    void allPlayersShareSameScoreAllInTiedCandidates() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        var result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(), result.definiteWinners());
        assertTrue(result.tiedCandidates().containsAll(List.of(p1, p2, p3)));
        assertEquals(3, result.tiedCandidates().size());
    }

    @Test
    void twoPlayersWithDistinctScoresBothInDefiniteWinners() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);

        var result = ranker.rank(List.of(p1, p2));

        assertEquals(new RankedResult(List.of(p1, p2), List.of()), result);
    }

    @Test
    void tieAtPosition3SplitsDefiniteWinnersAndTiedCandidates() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        var p4 = new PlayerAggregate(new Player("p4", "Diana"), 200);

        var result = ranker.rank(List.of(p1, p2, p3, p4));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertTrue(result.tiedCandidates().containsAll(List.of(p3, p4)));
        assertEquals(2, result.tiedCandidates().size());
    }
}
