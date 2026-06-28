package com.topthree.component;

import com.topthree.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class LeaderboardRankerTest {
    private LeaderboardRanker ranker;

    @BeforeEach
    void setup() {
        ranker = new LeaderboardRankerImpl();
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());
        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void singlePlayerGoesInDefiniteWinners() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        RankedResult result = ranker.rank(List.of(p1));

        assertThat(result.definiteWinners()).containsExactly(p1);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void twoPlayersGoInDefiniteWinnersDescending() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        RankedResult result = ranker.rank(List.of(p1, p2));

        assertThat(result.definiteWinners()).containsExactly(p1, p2);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void threeDistinctScoresAllInDefiniteWinners() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 300);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 200);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);
        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertThat(result.definiteWinners()).containsExactly(p1, p2, p3);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void fourPlayersTopThreeNoTie() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 100);
        RankedResult result = ranker.rank(List.of(p1, p2, p3, p4));

        assertThat(result.definiteWinners()).containsExactly(p1, p2, p3);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void tieAtPosition3SplitsDefiniteWinnersAndTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 200);
        RankedResult result = ranker.rank(List.of(p1, p2, p3, p4));

        assertThat(result.definiteWinners()).containsExactly(p1, p2);
        assertThat(result.tiedCandidates()).containsExactlyInAnyOrder(p3, p4);
    }

    @Test
    void allPlayersAllTiedGoesToTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);
        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).containsExactlyInAnyOrder(p1, p2, p3);
    }
}
