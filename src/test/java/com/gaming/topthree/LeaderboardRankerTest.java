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

    @Test
    void singlePlayerGoesInDefiniteWinners() {
        RankedResult result = ranker.rank(List.of(player("p1", 100)));

        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(100);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void twoPlayersGoInDefiniteWinnersDescending() {
        RankedResult result = ranker.rank(List.of(player("p1", 100), player("p2", 200)));

        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(200, 100);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void threeDistinctScoresAllInDefiniteWinners() {
        RankedResult result = ranker.rank(List.of(
                player("p1", 100), player("p2", 300), player("p3", 200)));

        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(300, 200, 100);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void fourPlayersNoTieTakesTopThree() {
        RankedResult result = ranker.rank(List.of(
                player("p1", 400), player("p2", 300), player("p3", 200), player("p4", 100)));

        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(400, 300, 200);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void tieAtBoundarySplitsWinnersAndCandidates() {
        RankedResult result = ranker.rank(List.of(
                player("p1", 400), player("p2", 300), player("p3", 200), player("p4", 200)));

        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(400, 300);
        assertThat(result.tiedCandidates()).extracting(PlayerAggregate::totalScore)
                .containsExactly(200, 200);
    }

    @Test
    void allPlayersTiedGoInTiedCandidates() {
        RankedResult result = ranker.rank(List.of(
                player("p1", 100), player("p2", 100), player("p3", 100)));

        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).extracting(PlayerAggregate::totalScore)
                .containsExactly(100, 100, 100);
    }
}
