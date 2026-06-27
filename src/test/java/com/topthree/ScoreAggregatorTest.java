package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertTrue(aggregates.isEmpty());
    }
}
