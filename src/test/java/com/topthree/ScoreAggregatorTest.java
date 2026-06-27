package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    @Test
    void emptyInputReturnsEmptyList() {
        var result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(List.of(), ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value());
    }

    @Test
    void singleRecordProducesOnePlayerAggregateWithCorrectTotalScore() {
        var record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );

        var result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(new PlayerAggregate(new Player("p1", "Alice"), 100), aggregates.get(0));
    }

    @Test
    void twoRecordsForSamePlayerSumTheirWeightedScores() {
        var record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        ); // weighted = 2 * 50 = 100
        var record2 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g2", "Go", 3, 30)
        ); // weighted = 3 * 30 = 90

        var result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(new PlayerAggregate(new Player("p1", "Alice"), 190), aggregates.get(0));
    }
}
