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
