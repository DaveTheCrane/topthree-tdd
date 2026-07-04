package topthree.impl;

import topthree.interfaces.ScoreAggregator;
import topthree.models.AggregationError;
import topthree.models.PlayerAggregate;
import topthree.models.Result;
import topthree.models.ScoreRecord;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {
    
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();
    
    @Test
    void aggregate_emptyInput_returnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());
        
        assertTrue(result instanceof Result.Ok);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertTrue(aggregates.isEmpty());
    }
    
    @Test
    void aggregate_singleRecord_producesCorrectPlayerAggregate() {
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));
        
        assertTrue(result instanceof Result.Ok);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        
        assertEquals("p1", aggregate.playerId());
        assertEquals("Alice", aggregate.playerName());
        assertEquals(100, aggregate.totalScore()); // 2 * 50 = 100
    }
    
    @Test
    void aggregate_multipleRecordsSamePlayer_sumsWeightedScores() {
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50); // 100
        ScoreRecord record2 = new ScoreRecord("p1", "Alice", "g2", "Checkers", 3, 40); // 120
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result instanceof Result.Ok);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        
        assertEquals("p1", aggregate.playerId());
        assertEquals("Alice", aggregate.playerName());
        assertEquals(220, aggregate.totalScore()); // 100 + 120 = 220
    }
    
    @Test
    void aggregate_differentPlayers_produceSeparateAggregates() {
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50); // 100
        ScoreRecord record2 = new ScoreRecord("p2", "Bob", "g1", "Chess", 3, 40); // 120
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result instanceof Result.Ok);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        
        assertEquals(2, aggregates.size());
        
        // Check that both players are present
        boolean foundP1 = false;
        boolean foundP2 = false;
        for (PlayerAggregate agg : aggregates) {
            if (agg.playerId().equals("p1")) {
                assertEquals("Alice", agg.playerName());
                assertEquals(100, agg.totalScore());
                foundP1 = true;
            } else if (agg.playerId().equals("p2")) {
                assertEquals("Bob", agg.playerName());
                assertEquals(120, agg.totalScore());
                foundP2 = true;
            }
        }
        
        assertTrue(foundP1, "Should have found player p1");
        assertTrue(foundP2, "Should have found player p2");
    }
    
    @Test
    void aggregate_nameConflict_lastSeenDisplayNameWins() {
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);
        ScoreRecord record2 = new ScoreRecord("p1", "Alicia", "g2", "Checkers", 3, 40);
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));
        
        assertTrue(result instanceof Result.Ok);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        
        assertEquals("p1", aggregate.playerId());
        assertEquals("Alicia", aggregate.playerName()); // Last seen name should win
        assertEquals(220, aggregate.totalScore()); // 100 + 120 = 220
    }
}