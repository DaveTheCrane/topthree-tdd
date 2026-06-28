package com.topthree.component;

import com.topthree.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ScoreAggregatorTest {
    private ScoreAggregator aggregator;

    @BeforeEach
    void setup() {
        aggregator = new ScoreAggregatorImpl();
    }

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        assertThat(ok.value()).isEmpty();
    }

    @Test
    void singleRecordProducesCorrectPlayerAggregate() {
        ScoreRecord record = new ScoreRecord(
            new Player("p1", "Alice"),
            new GameEntry("g1", "Chess", 2, 50)
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        assertThat(aggregates).hasSize(1);
        PlayerAggregate agg = aggregates.get(0);
        assertThat(agg.player().playerId()).isEqualTo("p1");
        assertThat(agg.player().playerName()).isEqualTo("Alice");
        assertThat(agg.totalScore()).isEqualTo(100); // 2 * 50
    }

    @Test
    void multipleRecordsSamPlayerSumWeightedScores() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
            new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g2", "Checkers", 3, 40))
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).totalScore()).isEqualTo(220); // (2*50) + (3*40) = 100 + 120
    }

    @Test
    void differentPlayersProduceSeparateAggregates() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
            new ScoreRecord(new Player("p2", "Bob"), new GameEntry("g2", "Checkers", 3, 40))
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        assertThat(aggregates).hasSize(2);
        assertThat(aggregates).anyMatch(a -> a.player().playerId().equals("p1"));
        assertThat(aggregates).anyMatch(a -> a.player().playerId().equals("p2"));
    }

    @Test
    void lastSeenDisplayNameWins() {
        List<ScoreRecord> records = List.of(
            new ScoreRecord(new Player("p1", "Alice"), new GameEntry("g1", "Chess", 2, 50)),
            new ScoreRecord(new Player("p1", "Alicia"), new GameEntry("g2", "Checkers", 3, 40))
        );

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).player().playerName()).isEqualTo("Alicia");
    }
}
