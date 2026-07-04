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

    @Test
    void singleRecordProducesCorrectAggregate() {
        Result<List<PlayerAggregate>, AggregationError> result =
                aggregator.aggregate(List.of(record("p1", "Alice", "g1", 2, 50)));

        List<PlayerAggregate> aggregates = unwrap(result);
        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).player().playerId()).isEqualTo("p1");
        assertThat(aggregates.get(0).player().playerName()).isEqualTo("Alice");
        assertThat(aggregates.get(0).totalScore()).isEqualTo(100);
    }

    @Test
    void multipleRecordsForSamePlayerSumWeightedScores() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(
                record("p1", "Alice", "g1", 2, 50),
                record("p1", "Alice", "g2", 3, 40)));

        List<PlayerAggregate> aggregates = unwrap(result);
        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).totalScore()).isEqualTo(220);
    }

    @Test
    void differentPlayersProduceSeparateAggregates() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(
                record("p1", "Alice", "g1", 2, 50),
                record("p2", "Bob", "g1", 1, 30)));

        List<PlayerAggregate> aggregates = unwrap(result);
        assertThat(aggregates).hasSize(2);
        assertThat(aggregates).extracting(a -> a.player().playerId())
                .containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    void lastSeenDisplayNameWinsOnConflict() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(
                record("p1", "Alice", "g1", 2, 50),
                record("p1", "Alicia", "g2", 1, 10)));

        List<PlayerAggregate> aggregates = unwrap(result);
        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).player().playerName()).isEqualTo("Alicia");
    }
}
