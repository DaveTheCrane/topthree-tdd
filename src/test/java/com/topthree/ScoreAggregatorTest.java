package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);

        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(List.of(), aggregates);
    }

    @Test
    void singleRecordProducesPlayerAggregateWithCorrectTotalScore() {
        ScoreRecord record = new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);

        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(new Player("p1", "Alice"), aggregates.get(0).player());
        assertEquals(100, aggregates.get(0).totalScore());
    }
}
