package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());

        assertEquals(new RankedResult(List.of(), List.of()), result);
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinners() {
        PlayerAggregate alice = new PlayerAggregate(new Player("p1", "Alice"), 100);

        RankedResult result = ranker.rank(List.of(alice));

        assertEquals(new RankedResult(List.of(alice), List.of()), result);
    }

    @Test
    void threeDistinctScorePlayersAllInDefiniteWinnersDescending() {
        PlayerAggregate alice = new PlayerAggregate(new Player("p1", "Alice"), 300);
        PlayerAggregate bob = new PlayerAggregate(new Player("p2", "Bob"), 200);
        PlayerAggregate carol = new PlayerAggregate(new Player("p3", "Carol"), 100);

        RankedResult result = ranker.rank(List.of(bob, carol, alice));

        assertEquals(new RankedResult(List.of(alice, bob, carol), List.of()), result);
    }
}
