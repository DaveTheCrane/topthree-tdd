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

    @Test
    void threeDistinctScorePlayersAllInDefiniteWinnersDescending() {
        PlayerAggregate first = new PlayerAggregate(new Player("p1", "Alice"), 300);
        PlayerAggregate second = new PlayerAggregate(new Player("p2", "Bob"), 200);
        PlayerAggregate third = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        // Pass in non-descending order to ensure sorting is tested
        RankedResult result = ranker.rank(List.of(third, first, second));

        assertThat(result.definiteWinners()).containsExactly(first, second, third);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void fourPlayersNoTie_topThreeInDefiniteWinners_fourthExcluded() {
        PlayerAggregate first = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate second = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate third = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate fourth = new PlayerAggregate(new Player("p4", "Diana"), 100);

        RankedResult result = ranker.rank(List.of(fourth, second, first, third));

        assertThat(result.definiteWinners()).containsExactly(first, second, third);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void tieAtPosition3_splitsDefiniteWinnersAndTiedCandidates() {
        PlayerAggregate first = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate second = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate tied1 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate tied2 = new PlayerAggregate(new Player("p4", "Diana"), 200);

        RankedResult result = ranker.rank(List.of(tied2, first, tied1, second));

        assertThat(result.definiteWinners()).containsExactly(first, second);
        assertThat(result.tiedCandidates()).containsExactlyInAnyOrder(tied1, tied2);
    }

    @Test
    void allTied_definiteWinnersEmpty_allInTiedCandidates() {
        PlayerAggregate alice = new PlayerAggregate(new Player("p1", "Alice"), 100);
        PlayerAggregate bob = new PlayerAggregate(new Player("p2", "Bob"), 100);
        PlayerAggregate charlie = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        RankedResult result = ranker.rank(List.of(alice, bob, charlie));

        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).containsExactlyInAnyOrder(alice, bob, charlie);
    }
}
