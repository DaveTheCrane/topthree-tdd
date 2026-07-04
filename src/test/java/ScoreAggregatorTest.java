import org.junit.jupiter.api.Test;
import topthree.impl.ScoreAggregatorImpl;
import topthree.interfaces.ScoreAggregator;
import topthree.models.AggregationError;
import topthree.models.PlayerAggregate;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Feature: top-three-high-scores
class ScoreAggregatorTest {

    @Test
    void emptyInputReturnsEmptyList() {
        ScoreAggregator aggregator = new ScoreAggregatorImpl();

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertTrue(result.isOk());
        assertEquals(List.of(), result.get());
    }

    @Test
    void singleRecordProducesCorrectPlayerAggregate() {
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("p1", aggregates.get(0).playerId());
        assertEquals("Alice", aggregates.get(0).playerName());
        assertEquals(100, aggregates.get(0).totalScore());
    }

    @Test
    void multipleRecordsForSamePlayerSumWeightedScores() {
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);
        ScoreRecord record2 = new ScoreRecord("p1", "Alice", "g2", "Poker", 3, 40);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals(220, aggregates.get(0).totalScore());
    }

    @Test
    void differentPlayersProduceSeparateAggregates() {
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);
        ScoreRecord record2 = new ScoreRecord("p2", "Bob", "g2", "Poker", 3, 40);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(2, aggregates.size());
    }

    @Test
    void lastSeenDisplayNameWinsOnNameConflict() {
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        ScoreRecord record1 = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 50);
        ScoreRecord record2 = new ScoreRecord("p1", "Alicia", "g2", "Poker", 3, 40);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertTrue(result.isOk());
        List<PlayerAggregate> aggregates = result.get();
        assertEquals(1, aggregates.size());
        assertEquals("Alicia", aggregates.get(0).playerName());
    }
}
