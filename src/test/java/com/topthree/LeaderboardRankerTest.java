package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());

        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinners() {
        PlayerAggregate aggregate = new PlayerAggregate(new Player("p1", "Alice"), 100);

        RankedResult result = ranker.rank(List.of(aggregate));

        assertThat(result.definiteWinners()).containsExactly(aggregate);
        assertThat(result.tiedCandidates()).isEmpty();
    }
}
