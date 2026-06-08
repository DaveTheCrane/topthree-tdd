package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertThat(aggregates).isEmpty();
    }

    @Test
    void singleRecordProducesPlayerAggregateWithCorrectTotalScore() {
        ScoreRecord record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertThat(aggregates).hasSize(1);
        PlayerAggregate aggregate = aggregates.get(0);
        assertThat(aggregate.player()).isEqualTo(new Player("p1", "Alice"));
        assertThat(aggregate.totalScore()).isEqualTo(100); // 2 * 50
    }

    @Test
    void twoRecordsForDifferentPlayersProduceTwoAggregates() {
        ScoreRecord record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );
        ScoreRecord record2 = new ScoreRecord(
                new Player("p2", "Bob"),
                new GameEntry("g2", "Go", 3, 80)
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertThat(aggregates).hasSize(2);
    }

    @Test
    void twoRecordsForSamePlayerSumWeightedScores() {
        ScoreRecord record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );
        ScoreRecord record2 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g2", "Go", 3, 80)
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertThat(aggregates).hasSize(1);
        PlayerAggregate aggregate = aggregates.get(0);
        assertThat(aggregate.player()).isEqualTo(new Player("p1", "Alice"));
        assertThat(aggregate.totalScore()).isEqualTo(340); // (2*50) + (3*80) = 100 + 240
    }
}
