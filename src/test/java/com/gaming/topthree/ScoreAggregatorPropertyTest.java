package com.gaming.topthree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link ScoreAggregatorImpl} (Properties 8-9).
 */
class ScoreAggregatorPropertyTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    @SuppressWarnings("unchecked")
    private static List<PlayerAggregate> unwrap(Result<List<PlayerAggregate>, AggregationError> result) {
        assertThat(result).isInstanceOf(Result.Ok.class);
        return ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
    }

    // Records whose display name is a deterministic function of the player id,
    // so every record for a given id carries the same name (consistent-name inputs).
    @Provide
    Arbitrary<List<ScoreRecord>> consistentNameRecords() {
        Arbitrary<Integer> playerIdx = Arbitraries.integers().between(0, 4);
        Arbitrary<String> gameId = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5);
        Arbitrary<Integer> hours = Arbitraries.integers().between(0, 100);
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);
        Arbitrary<ScoreRecord> record = Combinators.combine(playerIdx, gameId, hours, score).as((idx, gid, h, s) ->
                new ScoreRecord(
                        new Player("p" + idx, "Player" + idx),
                        new GameEntry(gid, gid + "-name", h, s)));
        return record.list().ofMinSize(1).ofMaxSize(40);
    }

    // Feature: top-three-high-scores, Property 8: Aggregation correctness - count, total score, and name preservation
    @Property(tries = 1000)
    void aggregationCorrectness(
            @ForAll("consistentNameRecords") List<ScoreRecord> records) {
        List<PlayerAggregate> aggregates = unwrap(aggregator.aggregate(records));

        // Expected: one aggregate per distinct player id, with summed weighted scores.
        Set<String> distinctIds = new LinkedHashSet<>();
        Map<String, Integer> expectedTotals = new HashMap<>();
        for (ScoreRecord r : records) {
            String id = r.player().playerId();
            distinctIds.add(id);
            int weighted = r.gameEntry().hoursPlayed() * r.gameEntry().normalisedScore();
            expectedTotals.merge(id, weighted, Integer::sum);
        }

        assertThat(aggregates).hasSize(distinctIds.size());
        assertThat(aggregates).extracting(a -> a.player().playerId())
                .containsExactlyInAnyOrderElementsOf(distinctIds);
        for (PlayerAggregate aggregate : aggregates) {
            String id = aggregate.player().playerId();
            assertThat(aggregate.totalScore()).isEqualTo(expectedTotals.get(id));
            assertThat(aggregate.player().playerName()).isEqualTo("Player" + id.substring(1));
        }
    }

    // Records that all share one player id but carry arbitrary (possibly conflicting) display names.
    @Provide
    Arbitrary<List<ScoreRecord>> sharedIdVaryingNameRecords() {
        Arbitrary<String> name = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(6);
        Arbitrary<String> gameId = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5);
        Arbitrary<Integer> hours = Arbitraries.integers().between(0, 100);
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);
        Arbitrary<ScoreRecord> record = Combinators.combine(name, gameId, hours, score).as((n, gid, h, s) ->
                new ScoreRecord(
                        new Player("p1", n),
                        new GameEntry(gid, gid + "-name", h, s)));
        return record.list().ofMinSize(2).ofMaxSize(20);
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    @Property(tries = 1000)
    void lastSeenNameWins(
            @ForAll("sharedIdVaryingNameRecords") List<ScoreRecord> records) {
        List<PlayerAggregate> aggregates = unwrap(aggregator.aggregate(records));

        String expectedName = records.get(records.size() - 1).player().playerName();
        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).player().playerName()).isEqualTo(expectedName);
    }
}
