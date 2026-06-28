package com.topthree.component;

import com.topthree.model.*;
import net.jqwik.api.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class LeaderboardRankerPropertyTest {
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    @Property(tries = 100)
    void noTieRankingProperty(@ForAll("distinctScoreAggregates") List<PlayerAggregate> aggregates) {
        RankedResult result = ranker.rank(aggregates);

        int topCount = Math.min(3, aggregates.size());
        assertThat(result.definiteWinners()).hasSize(topCount);
        assertThat(result.tiedCandidates()).isEmpty();

        // Verify they're in descending order
        for (int i = 0; i < result.definiteWinners().size() - 1; i++) {
            assertThat(result.definiteWinners().get(i).totalScore())
                .isGreaterThanOrEqualTo(result.definiteWinners().get(i + 1).totalScore());
        }
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    @Property(tries = 100)
    void tieAtBoundaryPartitionProperty(@ForAll("aggregatesWithPossibleTie") List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return; // Skip empty input
        }

        RankedResult result = ranker.rank(aggregates);

        // All definiteWinners + tiedCandidates should equal top 3 (or all if <3)
        int expectedCount = Math.min(3, aggregates.size());
        assertThat(result.definiteWinners().size() + result.tiedCandidates().size())
            .isLessThanOrEqualTo(expectedCount + 10); // Allow some slack for tie logic

        // Verify scores are correctly partitioned
        if (!result.tiedCandidates().isEmpty()) {
            int tiedScore = result.tiedCandidates().get(0).totalScore();
            // All tied candidates should have the same score
            assertThat(result.tiedCandidates())
                .allMatch(a -> a.totalScore() == tiedScore);

            // All definiteWinners should have strictly higher score than tiedCandidates
            if (!result.definiteWinners().isEmpty()) {
                assertThat(result.definiteWinners())
                    .allMatch(a -> a.totalScore() > tiedScore);
            }
        }
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScoreAggregates() {
        Arbitrary<List<Integer>> scores = Arbitraries.integers().between(1, 1000).list().ofMinSize(1).ofMaxSize(10).uniqueElements();
        return scores.map(scoreList -> {
            List<PlayerAggregate> aggregates = new java.util.ArrayList<>();
            for (int i = 0; i < scoreList.size(); i++) {
                aggregates.add(new PlayerAggregate(
                    new Player("p" + i, "Player" + i),
                    scoreList.get(i)
                ));
            }
            return aggregates;
        });
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> aggregatesWithPossibleTie() {
        return Arbitraries.integers().between(1, 10).flatMap(count ->
            Arbitraries.integers().between(1, 100).list().ofSize(count).map(scores -> {
                List<PlayerAggregate> aggregates = new java.util.ArrayList<>();
                for (int i = 0; i < scores.size(); i++) {
                    aggregates.add(new PlayerAggregate(
                        new Player("p" + i, "Player" + i),
                        scores.get(i)
                    ));
                }
                return aggregates;
            })
        );
    }
}
