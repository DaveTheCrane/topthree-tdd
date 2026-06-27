package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    @Test
    void emptyInputReturnsEmptyList() {
        var result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);
        assertEquals(List.of(), ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value());
    }

    @Test
    void singleRecordProducesOnePlayerAggregateWithCorrectTotalScore() {
        var record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );

        var result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(new PlayerAggregate(new Player("p1", "Alice"), 100), aggregates.get(0));
    }

    @Test
    void differentPlayersProduceSeparateAggregates() {
        var record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        ); // weighted = 2 * 50 = 100
        var record2 = new ScoreRecord(
                new Player("p2", "Bob"),
                new GameEntry("g2", "Go", 3, 30)
        ); // weighted = 3 * 30 = 90

        var result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(2, aggregates.size());
        assertEquals(new PlayerAggregate(new Player("p1", "Alice"), 100), aggregates.get(0));
        assertEquals(new PlayerAggregate(new Player("p2", "Bob"), 90), aggregates.get(1));
    }

    @Test
    void lastSeenDisplayNameWins() {
        var record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        ); // weighted = 2 * 50 = 100
        var record2 = new ScoreRecord(
                new Player("p1", "Alicia"),
                new GameEntry("g2", "Go", 3, 30)
        ); // weighted = 3 * 30 = 90

        var result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alicia", aggregates.get(0).player().playerName());
    }

    @Test
    void playerDisplayNameIsPreserved() {
        var record = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        );

        var result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alice", aggregates.get(0).player().playerName());
    }

    @Test
    void twoRecordsForSamePlayerSumTheirWeightedScores() {
        var record1 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g1", "Chess", 2, 50)
        ); // weighted = 2 * 50 = 100
        var record2 = new ScoreRecord(
                new Player("p1", "Alice"),
                new GameEntry("g2", "Go", 3, 30)
        ); // weighted = 3 * 30 = 90

        var result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals(new PlayerAggregate(new Player("p1", "Alice"), 190), aggregates.get(0));
    }

    // Feature: top-three-high-scores, Property 8: Aggregation correctness — count, total score, and name preservation
    // **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8**
    @Property(tries = 1000)
    void aggregationCorrectness(@ForAll("consistentScoreRecords") List<ScoreRecord> records) {
        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // Group records by player id for expected values
        Map<String, List<ScoreRecord>> byPlayerId = records.stream()
                .collect(Collectors.groupingBy(r -> r.player().playerId()));

        // One PlayerAggregate per distinct player id
        assertEquals(byPlayerId.size(), aggregates.size());

        // Verify each aggregate
        Set<String> seenPlayerIds = new HashSet<>();
        for (PlayerAggregate agg : aggregates) {
            String pid = agg.player().playerId();
            assertFalse(seenPlayerIds.contains(pid), "Duplicate aggregate for player " + pid);
            seenPlayerIds.add(pid);

            List<ScoreRecord> playerRecords = byPlayerId.get(pid);
            assertNotNull(playerRecords, "Aggregate for unknown player " + pid);

            // totalScore = sum of (hoursPlayed * normalisedScore) for all that player's records
            int expectedTotal = playerRecords.stream()
                    .mapToInt(r -> r.gameEntry().hoursPlayed() * r.gameEntry().normalisedScore())
                    .sum();
            assertEquals(expectedTotal, agg.totalScore(),
                    "Total score mismatch for player " + pid);

            // Player name is preserved (consistent name in this test, so all records have same name)
            String expectedName = playerRecords.get(0).player().playerName();
            assertEquals(expectedName, agg.player().playerName(),
                    "Player name not preserved for " + pid);
        }
    }

    @Provide
    Arbitrary<List<ScoreRecord>> consistentScoreRecords() {
        // Generate 2-4 players, each with 1-3 games, consistent names per player id
        Arbitrary<Integer> playerCountArb = Arbitraries.integers().between(2, 4);

        return playerCountArb.flatMap(playerCount -> {
            // Create player ids and names upfront
            Arbitrary<List<String>> playerIdsArb = Arbitraries.strings()
                    .alpha().ofMinLength(1).ofMaxLength(8)
                    .list().ofSize(playerCount)
                    .filter(ids -> ids.stream().distinct().count() == playerCount);

            Arbitrary<List<String>> playerNamesArb = Arbitraries.strings()
                    .alpha().ofMinLength(1).ofMaxLength(10)
                    .list().ofSize(playerCount);

            return Combinators.combine(playerIdsArb, playerNamesArb).flatAs((playerIds, playerNames) -> {
                // For each player, generate 1-3 game records
                List<Arbitrary<List<ScoreRecord>>> playerRecordArbs = new ArrayList<>();
                for (int i = 0; i < playerCount; i++) {
                    String pid = playerIds.get(i);
                    String pname = playerNames.get(i);
                    Player player = new Player(pid, pname);

                    Arbitrary<List<ScoreRecord>> recordsForPlayer = Arbitraries.integers().between(1, 3)
                            .flatMap(gameCount -> {
                                Arbitrary<ScoreRecord> singleRecord = Combinators.combine(
                                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                                        Arbitraries.integers().between(1, 100),
                                        Arbitraries.integers().between(1, 100)
                                ).as((gameId, gameName, hours, score) ->
                                        new ScoreRecord(player, new GameEntry(gameId, gameName, hours, score))
                                );
                                return singleRecord.list().ofSize(gameCount);
                            });
                    playerRecordArbs.add(recordsForPlayer);
                }

                // Combine all player records into one flat list
                return combineRecordLists(playerRecordArbs);
            });
        });
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    // **Validates: Requirements 2.6, 2.8**
    @Property(tries = 1000)
    void lastSeenDisplayNameWinsOnNameConflict(@ForAll("conflictingNameRecords") List<ScoreRecord> records) {
        var result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // There should be exactly one aggregate (all records share same player id)
        assertEquals(1, aggregates.size());

        // The player name should be the name from the LAST record in the list
        String expectedName = records.get(records.size() - 1).player().playerName();
        assertEquals(expectedName, aggregates.get(0).player().playerName(),
                "Expected last-seen display name to win");
    }

    @Provide
    Arbitrary<List<ScoreRecord>> conflictingNameRecords() {
        // Generate 2-4 records for a single player id, each with a DIFFERENT display name
        Arbitrary<String> playerIdArb = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<Integer> recordCountArb = Arbitraries.integers().between(2, 4);

        return Combinators.combine(playerIdArb, recordCountArb).flatAs((playerId, recordCount) -> {
            // Generate exactly recordCount distinct names
            Arbitrary<List<String>> distinctNamesArb = Arbitraries.strings()
                    .alpha().ofMinLength(1).ofMaxLength(10)
                    .list().ofSize(recordCount)
                    .filter(names -> names.stream().distinct().count() == recordCount);

            return distinctNamesArb.flatMap(names -> {
                List<Arbitrary<ScoreRecord>> recordArbs = new ArrayList<>();
                for (int i = 0; i < recordCount; i++) {
                    String name = names.get(i);
                    Player player = new Player(playerId, name);
                    Arbitrary<ScoreRecord> recordArb = Combinators.combine(
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                            Arbitraries.integers().between(1, 100),
                            Arbitraries.integers().between(1, 100)
                    ).as((gameId, gameName, hours, score) ->
                            new ScoreRecord(player, new GameEntry(gameId, gameName, hours, score))
                    );
                    recordArbs.add(recordArb);
                }

                // Combine all record arbitraries into a single list
                Arbitrary<List<ScoreRecord>> combined = recordArbs.get(0).map(r -> {
                    List<ScoreRecord> list = new ArrayList<>();
                    list.add(r);
                    return list;
                });
                for (int i = 1; i < recordArbs.size(); i++) {
                    combined = Combinators.combine(combined, recordArbs.get(i)).as((acc, r) -> {
                        acc.add(r);
                        return acc;
                    });
                }
                return combined.map(Collections::unmodifiableList);
            });
        });
    }

    private Arbitrary<List<ScoreRecord>> combineRecordLists(List<Arbitrary<List<ScoreRecord>>> arbs) {
        if (arbs.isEmpty()) {
            return Arbitraries.just(List.of());
        }
        Arbitrary<List<ScoreRecord>> combined = arbs.get(0).map(ArrayList::new);
        for (int i = 1; i < arbs.size(); i++) {
            combined = Combinators.combine(combined, arbs.get(i)).as((acc, next) -> {
                acc.addAll(next);
                return acc;
            });
        }
        return combined.map(Collections::unmodifiableList);
    }
}
