package com.topthree;

import net.jqwik.api.*;
import java.util.*;
import java.util.stream.Collectors;

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
}
