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
}