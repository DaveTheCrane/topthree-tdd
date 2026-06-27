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

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    /**
     * Validates: Requirements 2.6, 2.8
     */
    @Property(tries = 1000)
    void lastSeenDisplayNameWinsOnNameConflict(@ForAll("scoreRecordsWithNameConflict") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // Find the conflicting player id (the one that appears with different names)
        // and determine the last-seen name for that player
        Map<String, String> lastSeenNameById = new HashMap<>();
        for (ScoreRecord record : records) {
            lastSeenNameById.put(record.player().playerId(), record.player().playerName());
        }

        Map<String, PlayerAggregate> aggregateById = aggregates.stream()
                .collect(Collectors.toMap(a -> a.player().playerId(), a -> a));

        // Verify that for every player, the aggregate carries the last-seen name
        for (Map.Entry<String, String> entry : lastSeenNameById.entrySet()) {
            String playerId = entry.getKey();
            String expectedName = entry.getValue();
            assertTrue(aggregateById.containsKey(playerId),
                    "Missing aggregate for player id: " + playerId);
            assertEquals(expectedName, aggregateById.get(playerId).player().playerName(),
                    "Last-seen name should win for player: " + playerId);
        }
    }

    @Provide
    Arbitrary<List<ScoreRecord>> scoreRecordsWithNameConflict() {
        // Generate a player id that will have a name conflict
        Arbitrary<String> conflictPlayerIdArb = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8)
                .map(s -> "conflict_" + s);

        // Generate at least 2 different names for the conflicting player
        Arbitrary<List<String>> conflictNamesArb = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                .list().ofMinSize(2).ofMaxSize(5)
                .filter(names -> names.stream().distinct().count() >= 2);

        // Generate additional records for other players (0-3 other players)
        Arbitrary<Integer> otherPlayerCountArb = Arbitraries.integers().between(0, 3);

        return Combinators.combine(conflictPlayerIdArb, conflictNamesArb, otherPlayerCountArb)
                .flatAs((conflictPlayerId, conflictNames, otherCount) -> {
                    // Create records for the conflict player (one per name)
                    List<Arbitrary<ScoreRecord>> conflictRecordArbs = new ArrayList<>();
                    for (String name : conflictNames) {
                        Player player = new Player(conflictPlayerId, name);
                        Arbitrary<ScoreRecord> recordArb = Combinators.combine(
                                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                                Arbitraries.integers().between(1, 1000),
                                Arbitraries.integers().between(1, 100)
                        ).as((gameId, gameName, hours, score) ->
                                new ScoreRecord(player, new GameEntry(gameId, gameName, hours, score))
                        );
                        conflictRecordArbs.add(recordArb);
                    }

                    // Create records for other players
                    List<Arbitrary<List<ScoreRecord>>> otherRecordArbs = new ArrayList<>();
                    for (int i = 0; i < otherCount; i++) {
                        int idx = i;
                        Arbitrary<List<ScoreRecord>> otherPlayerRecords = Combinators.combine(
                                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                                Arbitraries.integers().between(1, 3)
                        ).flatAs((otherName, numGames) -> {
                            Player otherPlayer = new Player("other_" + idx, otherName);
                            List<Arbitrary<ScoreRecord>> gameArbs = new ArrayList<>();
                            for (int g = 0; g < numGames; g++) {
                                Arbitrary<ScoreRecord> gameArb = Combinators.combine(
                                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                                        Arbitraries.integers().between(1, 1000),
                                        Arbitraries.integers().between(1, 100)
                                ).as((gId, gName, hrs, scr) ->
                                        new ScoreRecord(otherPlayer, new GameEntry(gId, gName, hrs, scr))
                                );
                                gameArbs.add(gameArb);
                            }
                            return Combinators.combine(gameArbs).as(list -> list);
                        });
                        otherRecordArbs.add(otherPlayerRecords);
                    }

                    // Combine conflict records and other records, keeping conflict records in order
                    Arbitrary<List<ScoreRecord>> conflictListArb = Combinators.combine(conflictRecordArbs).as(list -> list);

                    if (otherRecordArbs.isEmpty()) {
                        return conflictListArb;
                    }

                    Arbitrary<List<ScoreRecord>> otherListArb = Combinators.combine(otherRecordArbs)
                            .as(lists -> {
                                List<ScoreRecord> all = new ArrayList<>();
                                for (List<ScoreRecord> list : lists) {
                                    all.addAll(list);
                                }
                                return all;
                            });

                    // Combine: put other records first, conflict records at end
                    // This ensures the last occurrence of the conflict player has the last name in conflictNames
                    return Combinators.combine(otherListArb, conflictListArb).as((others, conflicts) -> {
                        List<ScoreRecord> all = new ArrayList<>(others);
                        all.addAll(conflicts);
                        return all;
                    });
                });
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
