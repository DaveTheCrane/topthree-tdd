package com.topthree;

import com.topthree.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorPropertyTest {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    // --- Generators ---

    @Provide
    Arbitrary<String> playerIds() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                .map(s -> "p" + s);
    }

    @Provide
    Arbitrary<String> playerNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
    }

    @Provide
    Arbitrary<String> gameIds() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                .map(s -> "g" + s);
    }

    @Provide
    Arbitrary<ScoreRecord> scoreRecords() {
        return Combinators.combine(
                playerIds(),
                playerNames(),
                gameIds(),
                playerNames(),
                Arbitraries.integers().between(1, 100),
                Arbitraries.integers().between(1, 100)
        ).as((pid, pname, gid, gname, hours, score) ->
                new ScoreRecord(
                        new Player(pid, pname),
                        new GameEntry(gid, gname, hours, score)
                )
        );
    }

    @Provide
    Arbitrary<List<ScoreRecord>> scoreRecordLists() {
        return scoreRecords().list().ofMinSize(1).ofMaxSize(20);
    }

    // --- Property Tests ---

    // Feature: top-three-high-scores, Property 8: Aggregation correctness — one aggregate per player-id, correct totalScore, name preserved
    @Property(tries = 1000)
    void aggregationCorrectness(@ForAll("scoreRecordLists") List<ScoreRecord> records) {
        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // One aggregate per distinct player-id
        Set<String> distinctPlayerIds = records.stream()
                .map(r -> r.player().playerId())
                .collect(Collectors.toSet());
        assertEquals(distinctPlayerIds.size(), aggregates.size());

        // Each aggregate has correct totalScore
        Map<String, Integer> expectedScores = new HashMap<>();
        for (ScoreRecord record : records) {
            String pid = record.player().playerId();
            int weighted = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            expectedScores.merge(pid, weighted, Integer::sum);
        }

        for (PlayerAggregate agg : aggregates) {
            assertEquals(expectedScores.get(agg.player().playerId()), agg.totalScore(),
                    "Wrong totalScore for player " + agg.player().playerId());
        }

        // Player name preserved (last-seen for consistent names — tested more specifically in Property 9)
        for (PlayerAggregate agg : aggregates) {
            assertNotNull(agg.player().playerName());
            assertFalse(agg.player().playerName().isEmpty());
        }
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    @Property(tries = 1000)
    void lastSeenDisplayNameWins(
            @ForAll("playerIds") String playerId,
            @ForAll("playerNames") String firstName,
            @ForAll("playerNames") String secondName,
            @ForAll("gameIds") String gameId1,
            @ForAll("gameIds") String gameId2
    ) {
        Assume.that(!firstName.equals(secondName));
        Assume.that(!gameId1.equals(gameId2));

        var records = List.of(
                new ScoreRecord(new Player(playerId, firstName), new GameEntry(gameId1, "Game1", 2, 50)),
                new ScoreRecord(new Player(playerId, secondName), new GameEntry(gameId2, "Game2", 3, 40))
        );

        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(secondName, aggregates.get(0).player().playerName(),
                "Expected last-seen name '" + secondName + "' but got '" + aggregates.get(0).player().playerName() + "'");
    }
}
