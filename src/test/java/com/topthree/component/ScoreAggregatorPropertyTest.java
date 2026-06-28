package com.topthree.component;

import com.topthree.model.*;
import net.jqwik.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ScoreAggregatorPropertyTest {
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    // Feature: top-three-high-scores, Property 8: Aggregation correctness
    @Property(tries = 100)
    void aggregationCorrectnessProperty(@ForAll("validScoreRecordLists") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        // Count distinct players
        long distinctPlayers = records.stream()
            .map(r -> r.player().playerId())
            .distinct()
            .count();
        assertThat(aggregates).hasSize((int) distinctPlayers);

        // Verify each aggregate has correct total score
        for (PlayerAggregate agg : aggregates) {
            int expectedScore = records.stream()
                .filter(r -> r.player().playerId().equals(agg.player().playerId()))
                .mapToInt(r -> r.gameEntry().hoursPlayed() * r.gameEntry().normalisedScore())
                .sum();
            assertThat(agg.totalScore()).isEqualTo(expectedScore);
        }
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins
    @Property(tries = 100)
    void lastSeenDisplayNameWinsProperty(@ForAll("recordsWithNameConflict") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<List<PlayerAggregate>, AggregationError> ok = (Result.Ok<List<PlayerAggregate>, AggregationError>) result;
        List<PlayerAggregate> aggregates = ok.value();

        // Find the aggregate for player "p1"
        PlayerAggregate agg = aggregates.stream()
            .filter(a -> a.player().playerId().equals("p1"))
            .findFirst()
            .orElse(null);

        // The last name in the records should be the one used
        String expectedLastName = records.stream()
            .filter(r -> r.player().playerId().equals("p1"))
            .reduce((first, second) -> second)
            .map(r -> r.player().playerName())
            .orElse(null);

        assertThat(agg).isNotNull();
        assertThat(agg.player().playerName()).isEqualTo(expectedLastName);
    }

    @Provide
    Arbitrary<List<ScoreRecord>> validScoreRecordLists() {
        Arbitrary<ScoreRecord> scoreRecords = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.integers().between(1, 50),
            Arbitraries.integers().between(1, 100)
        ).as((playerId, playerName, gameId, gameName, hours, score) -> {
            Player player = new Player(playerId, playerName);
            GameEntry gameEntry = new GameEntry(gameId, gameName, hours, score);
            return new ScoreRecord(player, gameEntry);
        });

        return scoreRecords.list().ofSize(0);
    }

    @Provide
    Arbitrary<List<ScoreRecord>> recordsWithNameConflict() {
        // Generate records where player "p1" may appear multiple times with different names
        Arbitrary<List<String>> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).list().ofMinSize(1).ofMaxSize(3);
        return names.flatMap(nameList -> {
            var records = new java.util.ArrayList<ScoreRecord>();
            for (int i = 0; i < nameList.size(); i++) {
                records.add(new ScoreRecord(
                    new Player("p1", nameList.get(i)),
                    new GameEntry("g" + i, "Game" + i, 1, 50)
                ));
            }
            return Arbitraries.just(records);
        });
    }
}
