package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    // Feature: top-three-high-scores, Requirement 2.7: Empty input returns empty list
    @Test
    void aggregate_returnsEmptyList_forEmptyInput() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());
        
        assertTrue(result.isOk());
        assertEquals(List.of(), result.get());
    }

    // Feature: top-three-high-scores, Requirement 2.1, 2.2, 2.3, 2.4: Single record produces correct aggregate
    @Test
    void aggregate_singleRecord_producesAggregateWithWeightedScore() {
        Player player = new Player("p1", "Alice");
        GameEntry game = new GameEntry("g1", "Chess", 2, 50);
        ScoreRecord record = new ScoreRecord(player, game);
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("p1", aggregates.get(0).player().playerId());
        assertEquals(100, aggregates.get(0).totalScore());  // 2 * 50 = 100
    }

    // Feature: top-three-high-scores, Requirement 2.5: Same player multiple records sum weighted scores
    @Test
    void aggregate_samePlayer_multipleRecords_sumWeightedScores() {
        Player player = new Player("p1", "Alice");
        ScoreRecord record1 = new ScoreRecord(player, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(player, new GameEntry("g2", "Checkers", 3, 60));
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("p1", aggregates.get(0).player().playerId());
        assertEquals(280, aggregates.get(0).totalScore());  // (2*50) + (3*60) = 100 + 180 = 280
    }

    // Feature: top-three-high-scores, Requirement 2.1: Different players produce separate aggregates
    @Test
    void aggregate_differentPlayers_producesSeparateAggregates() {
        Player player1 = new Player("p1", "Alice");
        Player player2 = new Player("p2", "Bob");
        ScoreRecord record1 = new ScoreRecord(player1, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(player2, new GameEntry("g2", "Checkers", 3, 60));
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(2, aggregates.size());
        assertEquals("p1", aggregates.get(0).player().playerId());
        assertEquals("p2", aggregates.get(1).player().playerId());
    }

    // Feature: top-three-high-scores, Requirement 2.6, 2.8: Last-seen name wins
    @Test
    void aggregate_samePlayer_differentNames_lastSeenNameWins() {
        Player player1 = new Player("p1", "Alice");
        Player player2 = new Player("p1", "Alicia");
        ScoreRecord record1 = new ScoreRecord(player1, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(player2, new GameEntry("g2", "Checkers", 3, 60));
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("Alicia", aggregates.get(0).player().playerName());
    }

    // Feature: top-three-high-scores, Property 8: Aggregation correctness
    @Test
    void aggregate_preservesCountAndTotalScoreAndName() {
        Player player1 = new Player("p1", "Alice");
        Player player2 = new Player("p2", "Bob");
        ScoreRecord record1 = new ScoreRecord(player1, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(player2, new GameEntry("g2", "Checkers", 3, 60));
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(2, aggregates.size());
        
        PlayerAggregate p1Aggregate = aggregates.stream()
            .filter(a -> a.player().playerId().equals("p1"))
            .findFirst()
            .orElse(null);
        PlayerAggregate p2Aggregate = aggregates.stream()
            .filter(a -> a.player().playerId().equals("p2"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(p1Aggregate);
        assertEquals(100, p1Aggregate.totalScore());
        assertNotNull(p2Aggregate);
        assertEquals(180, p2Aggregate.totalScore());
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    @Test
    void aggregate_multipleNameConflicts_lastSeenNameWins() {
        ScoreRecord record1 = new ScoreRecord(
            new Player("p1", "Alice"),
            new GameEntry("g1", "Chess", 2, 50)
        );
        ScoreRecord record2 = new ScoreRecord(
            new Player("p1", "Alicia"),
            new GameEntry("g2", "Checkers", 3, 60)
        );
        ScoreRecord record3 = new ScoreRecord(
            new Player("p1", "Alison"),
            new GameEntry("g3", "Cards", 1, 100)
        );
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2, record3));
        
        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("Alison", aggregates.get(0).player().playerName());
    }
}

