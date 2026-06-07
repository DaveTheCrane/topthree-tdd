package com.topthree;

import net.jqwik.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorPropertyTest {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    // Feature: top-three-high-scores, Property 8: Aggregation correctness — count, total score, and name preservation
    // **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8**
    @Property(tries = 1000)
    void aggregationCorrectness(
            @ForAll("nonEmptyScoreRecordsWithConsistentNames") List<ScoreRecord> records
    ) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertInstanceOf(Result.Ok.class, result);

        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // Compute expected values independently
        Map<String, String> expectedNames = new HashMap<>();
        Map<String, Integer> expectedScores = new HashMap<>();
        for (ScoreRecord record : records) {
            String pid = record.player().playerId();
            expectedNames.put(pid, record.player().playerName());
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            expectedScores.merge(pid, weightedScore, Integer::sum);
        }

        // Assert one aggregate per distinct player id
        assertEquals(expectedNames.size(), aggregates.size(),
                "Number of aggregates should equal number of distinct player ids");

        // Assert correct totalScore and name preserved for each player
        Map<String, PlayerAggregate> aggregateMap = new HashMap<>();
        for (PlayerAggregate agg : aggregates) {
            aggregateMap.put(agg.player().playerId(), agg);
        }

        for (String pid : expectedNames.keySet()) {
            assertTrue(aggregateMap.containsKey(pid),
                    "Missing aggregate for player id: " + pid);
            PlayerAggregate agg = aggregateMap.get(pid);
            assertEquals(expectedScores.get(pid), agg.totalScore(),
                    "totalScore mismatch for player id: " + pid);
            assertEquals(expectedNames.get(pid), agg.player().playerName(),
                    "Player name not preserved for player id: " + pid);
        }
    }

    @Provide
    Arbitrary<List<ScoreRecord>> nonEmptyScoreRecordsWithConsistentNames() {
        // Generate 1-5 distinct player ids, each with a consistent name and 1-3 records
        Arbitrary<Integer> playerCountArb = Arbitraries.integers().between(1, 5);

        return playerCountArb.flatMap(playerCount ->
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8)
                        .list().ofSize(playerCount).uniqueElements()
                        .flatMap(playerIds ->
                                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                                        .list().ofSize(playerCount)
                                        .flatMap(playerNames ->
                                                buildRecordsForPlayers(playerIds, playerNames)
                                        )
                        )
        );
    }

    private Arbitrary<List<ScoreRecord>> buildRecordsForPlayers(List<String> playerIds, List<String> playerNames) {
        List<Arbitrary<List<ScoreRecord>>> playerRecordArbs = new ArrayList<>();

        for (int i = 0; i < playerIds.size(); i++) {
            String pid = playerIds.get(i);
            String pname = playerNames.get(i);
            Arbitrary<List<ScoreRecord>> recordsForPlayer = gameEntryArbitrary()
                    .list().ofMinSize(1).ofMaxSize(3)
                    .map(entries -> entries.stream()
                            .map(entry -> new ScoreRecord(new Player(pid, pname), entry))
                            .toList()
                    );
            playerRecordArbs.add(recordsForPlayer);
        }

        // Combine all player record lists into a single flattened list
        return combineRecordLists(playerRecordArbs);
    }

    private Arbitrary<List<ScoreRecord>> combineRecordLists(List<Arbitrary<List<ScoreRecord>>> arbs) {
        if (arbs.isEmpty()) {
            return Arbitraries.just(List.of());
        }
        if (arbs.size() == 1) {
            return arbs.get(0);
        }

        Arbitrary<List<ScoreRecord>> combined = arbs.get(0);
        for (int i = 1; i < arbs.size(); i++) {
            combined = Combinators.combine(combined, arbs.get(i))
                    .as((list1, list2) -> {
                        List<ScoreRecord> merged = new ArrayList<>(list1);
                        merged.addAll(list2);
                        return merged;
                    });
        }
        return combined;
    }

    private Arbitrary<GameEntry> gameEntryArbitrary() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                Arbitraries.integers().between(1, 10),
                Arbitraries.integers().between(1, 100)
        ).as((gameId, gameName, hours, score) -> new GameEntry(gameId, gameName, hours, score));
    }
}
