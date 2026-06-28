package com.topthree;

import org.junit.jupiter.api.Test;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests and property-based tests for ScoreAggregator.
 */
class ScoreAggregatorTest {
    private static final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    // ─────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────

    @Test
    void testEmptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(new ArrayList<>());
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).isEmpty();
        }
    }

    @Test
    void testSingleRecordProducesOneAggregate() {
        ScoreRecord record = new ScoreRecord(
            new Player("alice", "Alice"),
            new GameEntry("g1", "Game1", 2, 50)
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(1);
            assertThat(ok.value().get(0).totalScore()).isEqualTo(100); // 2 * 50
            assertThat(ok.value().get(0).player().playerId()).isEqualTo("alice");
        }
    }

    @Test
    void testTwoRecordsSamePlayerSumWeightedScores() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("alice", "Alice"), new GameEntry("g1", "Game1", 2, 50)),
            new ScoreRecord(new Player("alice", "Alice"), new GameEntry("g2", "Game2", 3, 30))
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(1);
            assertThat(ok.value().get(0).totalScore()).isEqualTo(190); // 2*50 + 3*30
        }
    }

    @Test
    void testDifferentPlayersSeparateAggregates() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("alice", "Alice"), new GameEntry("g1", "Game1", 2, 50)),
            new ScoreRecord(new Player("bob", "Bob"), new GameEntry("g2", "Game2", 3, 30))
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(2);
            assertThat(ok.value().get(0).player().playerId()).isEqualTo("alice");
            assertThat(ok.value().get(1).player().playerId()).isEqualTo("bob");
        }
    }

    @Test
    void testLastSeenDisplayNameWins() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("alice", "Alice"), new GameEntry("g1", "Game1", 2, 50)),
            new ScoreRecord(new Player("alice", "Alicia"), new GameEntry("g2", "Game2", 3, 30))
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(1);
            assertThat(ok.value().get(0).player().playerName()).isEqualTo("Alicia");
        }
    }

    // ─────────────────────────────────────────────────────
    // PROPERTY-BASED TESTS
    // ─────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────
    // PROPERTY TESTS (jqwik @ 100 tries each)
    // ─────────────────────────────────────────────────────

    /**
     * Feature: top-three-high-scores, Property 8: Aggregation correctness — count, total score, and name preservation
     * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8
     */
    @Property(tries = 100)
    void property8_aggregationCorrectness(
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 5) String playerName,
            @ForAll @IntRange(min = 1, max = 10) int hours1,
            @ForAll @IntRange(min = 1, max = 100) int score1,
            @ForAll @IntRange(min = 1, max = 10) int hours2,
            @ForAll @IntRange(min = 1, max = 100) int score2
    ) {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(
                new Player(playerId, playerName),
                new GameEntry("g1", "Game1", hours1, score1)
            ),
            new ScoreRecord(
                new Player(playerId, playerName),
                new GameEntry("g2", "Game2", hours2, score2)
            )
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(1);
            int expectedTotal = hours1 * score1 + hours2 * score2;
            assertThat(ok.value().get(0).totalScore()).isEqualTo(expectedTotal);
            assertThat(ok.value().get(0).player().playerName()).isEqualTo(playerName);
        }
    }

    /**
     * Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
     * Validates: Requirements 2.6, 2.8
     */
    @Property(tries = 100)
    void property9_lastSeenNameWins(
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 5) String firstName,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 5) String secondName
    ) {
        Assume.that(!firstName.equals(secondName));
        
        List<ScoreRecord> records = List.of(
            new ScoreRecord(
                new Player(playerId, firstName),
                new GameEntry("g1", "Game1", 2, 50)
            ),
            new ScoreRecord(
                new Player(playerId, secondName),
                new GameEntry("g2", "Game2", 3, 30)
            )
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok) {
            assertThat(ok.value()).hasSize(1);
            assertThat(ok.value().get(0).player().playerName()).isEqualTo(secondName);
        }
    }
}
