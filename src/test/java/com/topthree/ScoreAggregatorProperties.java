package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreAggregatorProperties {

    private final ScoreAggregator aggregator = new DefaultScoreAggregator();

    // Feature: top-three-high-scores, Property 8: Aggregation correctness — count, total score, and name preservation
    // **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8**
    @Property(tries = 1000)
    void aggregationCorrectness(@ForAll("nonEmptyConsistentScoreRecords") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // Compute expected distinct player ids
        Set<String> distinctPlayerIds = records.stream()
                .map(r -> r.player().playerId())
                .collect(Collectors.toSet());

        // Number of aggregates == number of distinct player ids
        assertThat(aggregates).hasSize(distinctPlayerIds.size());

        // Build expected totals and names per player id
        Map<String, Integer> expectedTotals = new HashMap<>();
        Map<String, String> expectedNames = new HashMap<>();
        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();
            expectedTotals.merge(playerId, weightedScore, Integer::sum);
            expectedNames.put(playerId, record.player().playerName());
        }

        // For each aggregate, verify totalScore and name
        for (PlayerAggregate aggregate : aggregates) {
            String playerId = aggregate.player().playerId();

            assertThat(distinctPlayerIds).contains(playerId);
            assertThat(aggregate.totalScore()).isEqualTo(expectedTotals.get(playerId));
            assertThat(aggregate.player().playerName()).isEqualTo(expectedNames.get(playerId));
        }
    }

    // Feature: top-three-high-scores, Property 9: Last-seen display name wins on name conflict
    // **Validates: Requirements 2.6, 2.8**
    @Property(tries = 1000)
    void lastSeenDisplayNameWinsOnNameConflict(@ForAll("recordsWithNameConflict") List<ScoreRecord> records) {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(records);

        assertThat(result).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();

        // For each player id, find the last-seen name in the input list
        Map<String, String> lastSeenNames = new LinkedHashMap<>();
        for (ScoreRecord record : records) {
            lastSeenNames.put(record.player().playerId(), record.player().playerName());
        }

        // Assert every aggregate carries the last-seen name for its player id
        for (PlayerAggregate aggregate : aggregates) {
            String playerId = aggregate.player().playerId();
            assertThat(aggregate.player().playerName()).isEqualTo(lastSeenNames.get(playerId));
        }
    }

    @Provide
    Arbitrary<List<ScoreRecord>> recordsWithNameConflict() {
        // Generate a fixed player id with at least 2 different names
        Arbitrary<String> playerIdArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha()
                .map(s -> "conflict_" + s);
        Arbitrary<String> nameArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
        Arbitrary<String> gameIdArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
        Arbitrary<String> gameNameArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
        Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 100);
        Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

        return Combinators.combine(playerIdArb, nameArb.list().ofMinSize(2).ofMaxSize(5))
                .flatAs((playerId, names) -> {
                    // Generate 2-10 records for this player id, cycling through names
                    // ensuring at least two different names appear and the last record has a known name
                    Arbitrary<Integer> recordCountArb = Arbitraries.integers().between(2, 10);

                    return recordCountArb.flatMap(recordCount -> {
                        // Build a list of records where names vary
                        Arbitrary<List<ScoreRecord>> recordsArb = Combinators.combine(
                                gameIdArb.list().ofSize(recordCount),
                                gameNameArb.list().ofSize(recordCount),
                                hoursArb.list().ofSize(recordCount),
                                scoreArb.list().ofSize(recordCount),
                                Arbitraries.integers().between(0, names.size() - 1).list().ofSize(recordCount)
                        ).as((gameIds, gameNames, hours, scores, nameIndices) -> {
                            List<ScoreRecord> result2 = new ArrayList<>();
                            for (int i = 0; i < recordCount; i++) {
                                // Use the name at the selected index; ensure first and last have different names
                                int nameIdx = nameIndices.get(i);
                                // For first record, use index 0; for last, use a different index if possible
                                String name;
                                if (i == 0) {
                                    name = names.get(0);
                                } else if (i == recordCount - 1) {
                                    // Use a name different from the first to guarantee conflict
                                    name = names.get(names.size() - 1);
                                } else {
                                    name = names.get(nameIdx);
                                }
                                Player player = new Player(playerId, name);
                                GameEntry gameEntry = new GameEntry(gameIds.get(i), gameNames.get(i), hours.get(i), scores.get(i));
                                result2.add(new ScoreRecord(player, gameEntry));
                            }
                            return result2;
                        });

                        return recordsArb;
                    });
                });
    }

    @Provide
    Arbitrary<List<ScoreRecord>> nonEmptyConsistentScoreRecords() {
        // Generate 1-5 player ids, each with a fixed name
        return Arbitraries.integers().between(1, 5).flatMap(playerCount -> {
            // Build fixed player id/name pairs deterministically using index suffix
            Arbitrary<String> nameArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
            Arbitrary<String> idArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();

            // Generate a list of player id/name pairs
            Arbitrary<List<String[]>> playersArb = Combinators.combine(
                    idArb.list().ofSize(playerCount),
                    nameArb.list().ofSize(playerCount)
            ).as((ids, names) -> {
                List<String[]> players = new ArrayList<>();
                for (int i = 0; i < playerCount; i++) {
                    players.add(new String[]{ids.get(i) + "_" + i, names.get(i)});
                }
                return players;
            });

            return playersArb.flatMap(players -> {
                // Generate 1-20 records, each randomly assigned to one of the players
                Arbitrary<Integer> playerIndexArb = Arbitraries.integers().between(0, players.size() - 1);
                Arbitrary<String> gameIdArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
                Arbitrary<String> gameNameArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha();
                Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 100);
                Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

                Arbitrary<ScoreRecord> recordArb = Combinators.combine(
                        playerIndexArb, gameIdArb, gameNameArb, hoursArb, scoreArb
                ).as((playerIdx, gameId, gameName, hours, score) -> {
                    String[] playerData = players.get(playerIdx);
                    Player player = new Player(playerData[0], playerData[1]);
                    GameEntry gameEntry = new GameEntry(gameId, gameName, hours, score);
                    return new ScoreRecord(player, gameEntry);
                });

                return recordArb.list().ofMinSize(1).ofMaxSize(20);
            });
        });
    }
}
