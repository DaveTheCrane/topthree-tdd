package com.topthree;

import net.jqwik.api.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardRankerProperties {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    // **Validates: Requirements 3.2, 3.4**
    @Property(tries = 1000)
    void noTieRankingPlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScoreAggregates") List<PlayerAggregate> aggregates) {

        RankedResult result = ranker.rank(aggregates);

        int n = aggregates.size();
        int expectedWinnerCount = Math.min(3, n);

        // definiteWinners has exactly min(3, N) players
        assertThat(result.definiteWinners()).hasSize(expectedWinnerCount);

        // tiedCandidates is empty
        assertThat(result.tiedCandidates()).isEmpty();

        // definiteWinners are in descending totalScore order
        List<PlayerAggregate> winners = result.definiteWinners();
        for (int i = 0; i < winners.size() - 1; i++) {
            assertThat(winners.get(i).totalScore())
                    .isGreaterThan(winners.get(i + 1).totalScore());
        }

        // definiteWinners are the top min(3, N) players by score
        List<Integer> allScoresDescending = aggregates.stream()
                .map(PlayerAggregate::totalScore)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        List<Integer> winnerScores = winners.stream()
                .map(PlayerAggregate::totalScore)
                .collect(Collectors.toList());

        assertThat(winnerScores).isEqualTo(allScoresDescending.subList(0, expectedWinnerCount));
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScoreAggregates() {
        // Generate 1-10 PlayerAggregates with ALL DISTINCT totalScores
        return Arbitraries.integers().between(1, 10).flatMap(count -> {
            // Generate 'count' distinct scores using a set approach
            Arbitrary<Set<Integer>> distinctScoresArb = Arbitraries.integers()
                    .between(1, 10000)
                    .set().ofSize(count);

            return distinctScoresArb.map(scoreSet -> {
                List<Integer> scores = new ArrayList<>(scoreSet);
                List<PlayerAggregate> aggregates = new ArrayList<>();
                for (int i = 0; i < scores.size(); i++) {
                    Player player = new Player("p" + i, "Player" + i);
                    aggregates.add(new PlayerAggregate(player, scores.get(i)));
                }
                return aggregates;
            });
        });
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    // **Validates: Requirements 3.3, 3.6**
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(
            @ForAll("tiedAtBoundaryAggregates") List<PlayerAggregate> aggregates) {

        RankedResult result = ranker.rank(aggregates);

        // Determine the boundary score: the score shared by 2+ players creating the tie
        // Since our generator guarantees at least 2 players share the boundary score,
        // we can compute it from the sorted list
        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        int highestScore = sorted.get(0).totalScore();
        boolean allSameScore = sorted.stream().allMatch(p -> p.totalScore() == highestScore);

        if (allSameScore) {
            // Degenerate case: all players share the same score
            // definiteWinners should be empty and all players in tiedCandidates
            assertThat(result.definiteWinners()).isEmpty();
            assertThat(result.tiedCandidates()).hasSize(aggregates.size());
            assertThat(new HashSet<>(result.tiedCandidates()))
                    .isEqualTo(new HashSet<>(aggregates));
        } else {
            // Find the boundary score: the score at index 2 (position 3) that is shared
            // by at least one player below it
            // Since our generator guarantees a tie at boundary, find the boundary score
            int boundaryScore = sorted.get(2).totalScore();

            // All players with score strictly above boundary are definiteWinners
            List<PlayerAggregate> expectedWinners = aggregates.stream()
                    .filter(p -> p.totalScore() > boundaryScore)
                    .collect(Collectors.toList());

            // All players sharing the boundary score are tiedCandidates
            List<PlayerAggregate> expectedTied = aggregates.stream()
                    .filter(p -> p.totalScore() == boundaryScore)
                    .collect(Collectors.toList());

            assertThat(result.definiteWinners()).hasSize(expectedWinners.size());
            assertThat(new HashSet<>(result.definiteWinners()))
                    .isEqualTo(new HashSet<>(expectedWinners));

            assertThat(result.tiedCandidates()).hasSize(expectedTied.size());
            assertThat(new HashSet<>(result.tiedCandidates()))
                    .isEqualTo(new HashSet<>(expectedTied));
        }
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> tiedAtBoundaryAggregates() {
        // Generate lists where at least 2 players share a boundary score.
        // Two strategies:
        // 1. All-same-score (degenerate case): 2+ players with the same score
        // 2. N > 3 with tie at boundary: 0-2 distinct scores above boundary,
        //    2+ players at boundary, ensuring total > 3 so boundary logic applies
        Arbitrary<List<PlayerAggregate>> allSameCase = Arbitraries.integers().between(2, 6)
                .flatMap(count -> Arbitraries.integers().between(1, 5000).map(score -> {
                    List<PlayerAggregate> aggregates = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        Player player = new Player("p" + i, "Player" + i);
                        aggregates.add(new PlayerAggregate(player, score));
                    }
                    return aggregates;
                }));

        Arbitrary<List<PlayerAggregate>> boundaryTieCase = Arbitraries.integers().between(0, 2)
                .flatMap(aboveCount -> {
                    // Ensure total players > 3: tiedCount must be > 3 - aboveCount
                    int minTied = Math.max(2, 4 - aboveCount);
                    return Arbitraries.integers().between(minTied, 5).flatMap(tiedCount ->
                        Arbitraries.integers().between(1, 5000).flatMap(boundaryScore -> {
                            Arbitrary<List<Integer>> aboveScoresArb;
                            if (aboveCount == 0) {
                                aboveScoresArb = Arbitraries.just(List.of());
                            } else {
                                aboveScoresArb = Arbitraries.integers()
                                        .between(boundaryScore + 1, boundaryScore + 5000)
                                        .set().ofSize(aboveCount)
                                        .map(s -> new ArrayList<>(s));
                            }

                            return aboveScoresArb.map(aboveScores -> {
                                List<PlayerAggregate> aggregates = new ArrayList<>();
                                int idx = 0;

                                // Add players with scores above boundary
                                for (int score : aboveScores) {
                                    Player player = new Player("p" + idx, "Player" + idx);
                                    aggregates.add(new PlayerAggregate(player, score));
                                    idx++;
                                }

                                // Add 2+ players tied at the boundary score
                                for (int i = 0; i < tiedCount; i++) {
                                    Player player = new Player("p" + idx, "Player" + idx);
                                    aggregates.add(new PlayerAggregate(player, boundaryScore));
                                    idx++;
                                }

                                // Shuffle to avoid order bias
                                Collections.shuffle(aggregates);
                                return aggregates;
                            });
                        })
                    );
                });

        return Arbitraries.oneOf(allSameCase, boundaryTieCase);
    }
}
