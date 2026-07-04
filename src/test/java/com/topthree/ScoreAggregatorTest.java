package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    @Test
    void differentPlayersProduceSeparateAggregates() {
        var records = List.of(
                new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
                new ScoreRecord(new Player("p2", "Bob"), new GameEntry("g2", "Go", 3, 40))
        );
        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(2, aggregates.size());
        assertEquals("p1", aggregates.get(0).player().playerId());
        assertEquals("p2", aggregates.get(1).player().playerId());
    }

    @Test
    void multipleRecordsSamePlayerSumsWeightedScores() {
        var records = List.of(
                new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
                new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g2", "Go", 3, 40))
        );
        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(220, aggregates.get(0).totalScore()); // (2*50) + (3*40) = 100 + 120 = 220
    }

    @Test
    void singleRecordProducesCorrectAggregate() {
        var record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );
        var result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("p1", aggregates.get(0).player().playerId());
        assertEquals("Alice", aggregates.get(0).player().playerName());
        assertEquals(100, aggregates.get(0).totalScore()); // 2 * 50 = 100
    }

    @Test
    void emptyInputReturnsEmptyList() {
        var result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertTrue(aggregates.isEmpty());
    }
}
