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
}
