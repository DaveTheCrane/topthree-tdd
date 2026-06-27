package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Test
    void singleRecordProducesOnePlayerAggregateWithCorrectTotalScore() {
        Player player = new Player("p1", "Alice");
        GameEntry game = new GameEntry("g1", "Chess", 2, 50);
        ScoreRecord record = new ScoreRecord(player, game);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        assertEquals(new Player("p1", "Alice"), aggregate.player());
        assertEquals(100, aggregate.totalScore());
    }

    @Test
    void twoRecordsForDifferentPlayersProduceTwoPlayerAggregates() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        ScoreRecord record1 = new ScoreRecord(p1, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(p2, new GameEntry("g2", "Poker", 3, 30));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(2, aggregates.size());
        assertEquals(new Player("p1", "Alice"), aggregates.get(0).player());
        assertEquals(new Player("p2", "Bob"), aggregates.get(1).player());
    }

    @Test
    void samePlayerIdWithDifferentDisplayNamesLastSeenNameWins() {
        Player firstEncounter = new Player("p1", "Alice");
        Player secondEncounter = new Player("p1", "Alicia");
        ScoreRecord record1 = new ScoreRecord(firstEncounter, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(secondEncounter, new GameEntry("g2", "Poker", 3, 30));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alicia", aggregates.get(0).player().playerName());
    }

    @Test
    void playerDisplayNameIsPreservedInPlayerAggregate() {
        Player player = new Player("p1", "Alice");
        ScoreRecord record = new ScoreRecord(player, new GameEntry("g1", "Chess", 2, 50));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alice", aggregates.get(0).player().playerName());
    }

    @Test
    void twoRecordsForSamePlayerSumTheirWeightedScores() {
        Player player = new Player("p1", "Alice");
        ScoreRecord record1 = new ScoreRecord(player, new GameEntry("g1", "Chess", 2, 50));   // weighted = 100
        ScoreRecord record2 = new ScoreRecord(player, new GameEntry("g2", "Poker", 3, 30));   // weighted = 90

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        assertEquals(new Player("p1", "Alice"), aggregate.player());
        assertEquals(190, aggregate.totalScore());
    }

    // Feature: top-three-high-scores, Property 8: Aggregation correctness — count, total score, and name preservation
    /**
     * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8
     */
    @Property(tries = 1000)
    void aggregationCorrectness(@ForAll("scoreRecordsWithConsistentNames") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // Compute expected values from input
        Map<String, String> expectedNameById = new HashMap<>();
        Map<String, Integer> expectedScoreById = new HashMap<>();
        for (ScoreRecord record : records) {
            String id = record.player().playerId();
            expectedNameById.put(id, record.player().playerName());
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            expectedScoreById.merge(id, weightedScore, Integer::sum);
        }

        // Number of aggregates equals number of distinct player ids
        assertEquals(expectedNameById.size(), aggregates.size(),
                "Number of aggregates should equal number of distinct player ids");

        // Each aggregate has correct totalScore and preserved playerName
        Map<String, PlayerAggregate> aggregateById = aggregates.stream()
                .collect(Collectors.toMap(a -> a.player().playerId(), a -> a));

        for (String playerId : expectedNameById.keySet()) {
            assertTrue(aggregateById.containsKey(playerId),
                    "Missing aggregate for player id: " + playerId);
            PlayerAggregate aggregate = aggregateById.get(playerId);
            assertEquals(expectedScoreById.get(playerId), aggregate.totalScore(),
                    "Total score mismatch for player: " + playerId);
            assertEquals(expectedNameById.get(playerId), aggregate.player().playerName(),
                    "Player name mismatch for player: " + playerId);
        }
    }

    @Provide
    Arbitrary<List<ScoreRecord>> scoreRecordsWithConsistentNames() {
        // Generate 1-5 distinct players, each with a consistent name
        Arbitrary<Integer> playerCount = Arbitraries.integers().between(1, 5);

        return playerCount.flatMap(numPlayers -> {
            // Generate player ids and names upfront
            Arbitrary<List<Tuple2<String, String>>> playersArb = Arbitraries.just(numPlayers)
                    .flatMap(n -> {
                        List<Arbitrary<Tuple2<String, String>>> playerArbs = new ArrayList<>();
                        for (int i = 0; i < n; i++) {
                            String playerId = "player" + (i + 1);
                            Arbitrary<String> nameArb = Arbitraries.strings()
                                    .alpha().ofMinLength(1).ofMaxLength(10);
                            playerArbs.add(nameArb.map(name -> Tuple.of(playerId, name)));
                        }
                        return Combinators.combine(playerArbs).as(list -> list);
                    });

            return playersArb.flatMap(players -> {
                // For each player, generate 1-5 game entries
                List<Arbitrary<List<ScoreRecord>>> perPlayerRecords = new ArrayList<>();
                for (int i = 0; i < players.size(); i++) {
                    String playerId = players.get(i).get1();
                    String playerName = players.get(i).get2();
                    Player player = new Player(playerId, playerName);

                    int playerIndex = i;
                    Arbitrary<List<ScoreRecord>> recordsForPlayer =
                            Arbitraries.integers().between(1, 5).flatMap(numGames -> {
                                List<Arbitrary<ScoreRecord>> gameArbs = new ArrayList<>();
                                for (int g = 0; g < numGames; g++) {
                                    String gameId = "game_p" + (playerIndex + 1) + "_" + (g + 1);
                                    Arbitrary<ScoreRecord> recordArb = Combinators.combine(
                                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                                            Arbitraries.integers().between(1, 1000),
                                            Arbitraries.integers().between(1, 100)
                                    ).as((gameName, hours, score) ->
                                            new ScoreRecord(player, new GameEntry(gameId, gameName, hours, score))
                                    );
                                    gameArbs.add(recordArb);
                                }
                                return Combinators.combine(gameArbs).as(list -> list);
                            });

                    perPlayerRecords.add(recordsForPlayer);
                }

                return Combinators.combine(perPlayerRecords).as(lists -> {
                    List<ScoreRecord> all = new ArrayList<>();
                    for (List<ScoreRecord> list : lists) {
                        all.addAll(list);
                    }
                    return all;
                });
            });
        });
    }
}
