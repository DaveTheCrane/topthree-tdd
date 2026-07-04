package com.gaming.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    private static ScoreRecord record(String playerId, String playerName, String gameId, int hours, int score) {
        return new ScoreRecord(
                new Player(playerId, playerName),
                new GameEntry(gameId, gameId + "-name", hours, score));
    }

    @SuppressWarnings("unchecked")
    private static List<PlayerAggregate> unwrap(Result<List<PlayerAggregate>, AggregationError> result) {
        assertThat(result).isInstanceOf(Result.Ok.class);
        return ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
    }

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertThat(unwrap(result)).isEmpty();
    }
}
