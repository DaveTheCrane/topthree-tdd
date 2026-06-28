package com.topthree;

import org.junit.jupiter.api.Test;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests and property-based tests for LeaderboardRanker.
 */
class LeaderboardRankerTest {
    private static final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    // ─────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────

    @Test
    void testEmptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(new ArrayList<>());
        
        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void testSinglePlayerGoesIntoDefiniteWinners() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 100)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(1);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    @Test
    void testThreeDistinctScoresAllInDefiniteWinners() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 300),
            new PlayerAggregate(new Player("bob", "Bob"), 200),
            new PlayerAggregate(new Player("charlie", "Charlie"), 100)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(3);
        assertThat(result.tiedCandidates()).isEmpty();
        assertThat(result.definiteWinners().get(0).player().playerId()).isEqualTo("alice");
        assertThat(result.definiteWinners().get(1).player().playerId()).isEqualTo("bob");
        assertThat(result.definiteWinners().get(2).player().playerId()).isEqualTo("charlie");
    }

    @Test
    void testFourPlayersNoTieExcludesFourth() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 400),
            new PlayerAggregate(new Player("bob", "Bob"), 300),
            new PlayerAggregate(new Player("charlie", "Charlie"), 200),
            new PlayerAggregate(new Player("dave", "Dave"), 100)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(3);
        assertThat(result.tiedCandidates()).isEmpty();
        assertThat(result.definiteWinners().stream().map(p -> p.player().playerId()).toList())
            .containsExactly("alice", "bob", "charlie");
    }

    @Test
    void testTieAtBoundaryPartitionCorrectly() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 400),
            new PlayerAggregate(new Player("bob", "Bob"), 300),
            new PlayerAggregate(new Player("charlie", "Charlie"), 200),
            new PlayerAggregate(new Player("dave", "Dave"), 200)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(2);
        assertThat(result.tiedCandidates()).hasSize(2);
        assertThat(result.tiedCandidates().stream().map(p -> p.totalScore()).toList())
            .allMatch(s -> s == 200);
    }

    @Test
    void testAllTiedDefiniteWinnersEmpty() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 100),
            new PlayerAggregate(new Player("bob", "Bob"), 100),
            new PlayerAggregate(new Player("charlie", "Charlie"), 100)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).isEmpty();
        assertThat(result.tiedCandidates()).hasSize(3);
    }

    @Test
    void testTwoPlayersBothInDefiniteWinners() {
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("alice", "Alice"), 200),
            new PlayerAggregate(new Player("bob", "Bob"), 100)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(2);
        assertThat(result.tiedCandidates()).isEmpty();
    }

    // ─────────────────────────────────────────────────────
    // PROPERTY TESTS (jqwik @ 100 tries each)
    // ─────────────────────────────────────────────────────

    /**
     * Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
     * Validates: Requirements 3.2, 3.4
     */
    @Property(tries = 100)
    void property10_noTieRanking(
            @ForAll @IntRange(min = 100, max = 500) int score1,
            @ForAll @IntRange(min = 100, max = 500) int score2,
            @ForAll @IntRange(min = 100, max = 500) int score3
    ) {
        Assume.that(score1 != score2 && score2 != score3 && score1 != score3);
        
        List<PlayerAggregate> aggregates = List.of(
            new PlayerAggregate(new Player("p1", "P1"), score1),
            new PlayerAggregate(new Player("p2", "P2"), score2),
            new PlayerAggregate(new Player("p3", "P3"), score3)
        );
        
        RankedResult result = ranker.rank(aggregates);
        
        assertThat(result.definiteWinners()).hasSize(3);
        assertThat(result.tiedCandidates()).isEmpty();
        // Verify descending order
        assertThat(result.definiteWinners().get(0).totalScore())
            .isGreaterThanOrEqualTo(result.definiteWinners().get(1).totalScore());
        assertThat(result.definiteWinners().get(1).totalScore())
            .isGreaterThanOrEqualTo(result.definiteWinners().get(2).totalScore());
    }

    /**
     * Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
     * Validates: Requirements 3.3, 3.6
     */
    @Property(tries = 100)
    void property11_tiePartition(
            @ForAll @IntRange(min = 100, max = 200) int boundaryScore
    ) {
        List<PlayerAggregate> aggregates = new ArrayList<>();
        aggregates.add(new PlayerAggregate(new Player("p1", "P1"), 500));
        aggregates.add(new PlayerAggregate(new Player("p2", "P2"), 300));
        aggregates.add(new PlayerAggregate(new Player("p3", "P3"), boundaryScore));
        aggregates.add(new PlayerAggregate(new Player("p4", "P4"), boundaryScore));
        
        RankedResult result = ranker.rank(aggregates);
        
        // Should have boundary score tied
        assertThat(result.tiedCandidates().stream().map(p -> p.totalScore()).distinct())
            .contains(boundaryScore);
    }
}
