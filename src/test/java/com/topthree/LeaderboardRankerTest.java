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
}
