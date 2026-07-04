package com.gaming.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    private static PlayerAggregate player(String id, int totalScore) {
        return new PlayerAggregate(new Player(id, id + "-name"), totalScore);
    }

    @Test
    void emptyInputReturnsEmptyResult() {
        RankedResult result = ranker.rank(List.of());

        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).isEmpty();
    }
}
