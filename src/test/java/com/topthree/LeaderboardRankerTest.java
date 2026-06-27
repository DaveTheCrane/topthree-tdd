package com.topthree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private LeaderboardRanker ranker;

    @BeforeEach
    void setUp() {
        ranker = new LeaderboardRankerImpl();
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());

        assertNotNull(result);
        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinners() {
        PlayerAggregate player = new PlayerAggregate(new Player("p1", "Alice"), 100);

        RankedResult result = ranker.rank(List.of(player));

        assertEquals(List.of(player), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void threeDistinctScorePlayersAllInDefiniteWinnersDescending() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 300);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 200);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        // Pass in non-sorted order to verify sorting
        RankedResult result = ranker.rank(List.of(p2, p3, p1));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void fourPlayersNoTieTopThreeInDefiniteWinnersFourthExcluded() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 100);

        RankedResult result = ranker.rank(List.of(p4, p2, p1, p3));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void tieAtPositionThreeSplitsDefiniteWinnersAndTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 200);

        RankedResult result = ranker.rank(List.of(p3, p1, p4, p2));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertEquals(List.of(p3, p4), result.tiedCandidates());
    }

    @Test
    void allPlayersSameScoreDefiniteWinnersEmptyAllInTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(p1, p2, p3), result.tiedCandidates());
    }
}
