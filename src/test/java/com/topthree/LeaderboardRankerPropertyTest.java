package com.topthree;

import com.topthree.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerPropertyTest {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    // --- Generators ---

    @Provide
    Arbitrary<PlayerAggregate> playerAggregates() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5).map(s -> "p" + s),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                Arbitraries.integers().between(1, 10000)
        ).as((pid, pname, score) -> new PlayerAggregate(new Player(pid, pname), score));
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScoreAggregates() {
        return Arbitraries.integers().between(1, 10000)
                .set().ofMinSize(4).ofMaxSize(10)
                .map(scores -> {
                    List<PlayerAggregate> list = new ArrayList<>();
                    int i = 1;
                    for (int score : scores) {
                        list.add(new PlayerAggregate(new Player("p" + i, "Player" + i), score));
                        i++;
                    }
                    return list;
                });
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> tiedAtBoundaryAggregates() {
        // Generate aggregates where at least two players share a score at the boundary
        return Combinators.combine(
                Arbitraries.integers().between(500, 10000),  // score above boundary
                Arbitraries.integers().between(1, 499),      // boundary score
                Arbitraries.integers().between(2, 5)         // number of players at boundary
        ).as((aboveScore, boundaryScore, tiedCount) -> {
            List<PlayerAggregate> list = new ArrayList<>();
            // 1-2 players above boundary
            list.add(new PlayerAggregate(new Player("p1", "Above1"), aboveScore));
            list.add(new PlayerAggregate(new Player("p2", "Above2"), aboveScore - 1));
            // Multiple players at boundary score
            for (int i = 0; i < tiedCount; i++) {
                list.add(new PlayerAggregate(new Player("pt" + i, "Tied" + i), boundaryScore));
            }
            return list;
        });
    }

    // --- Property Tests ---

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definiteWinners
    @Property(tries = 1000)
    void noTieRankingPlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScoreAggregates") List<PlayerAggregate> aggregates
    ) {
        var result = ranker.rank(aggregates);

        // All scores are distinct, so no tie at boundary
        // definiteWinners should have min(3, N) players in descending order
        int expectedWinners = Math.min(3, aggregates.size());
        assertEquals(expectedWinners, result.definiteWinners().size());
        assertTrue(result.tiedCandidates().isEmpty());

        // Verify descending order
        for (int i = 0; i < result.definiteWinners().size() - 1; i++) {
            assertTrue(result.definiteWinners().get(i).totalScore() >= result.definiteWinners().get(i + 1).totalScore());
        }

        // Verify these are the actual top 3 scores
        List<Integer> allScores = aggregates.stream()
                .map(PlayerAggregate::totalScore)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        for (int i = 0; i < expectedWinners; i++) {
            assertEquals(allScores.get(i), result.definiteWinners().get(i).totalScore());
        }
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(
            @ForAll("tiedAtBoundaryAggregates") List<PlayerAggregate> aggregates
    ) {
        var result = ranker.rank(aggregates);

        // Determine boundary score: sort descending and check position 2
        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        int boundaryScore = sorted.get(2).totalScore();
        boolean hasTieAtBoundary = sorted.size() > 3 && sorted.get(3).totalScore() == boundaryScore;

        if (hasTieAtBoundary) {
            // All definiteWinners should have score > boundaryScore
            for (PlayerAggregate winner : result.definiteWinners()) {
                assertTrue(winner.totalScore() > boundaryScore,
                        "definiteWinner has score " + winner.totalScore() + " which is not > boundary " + boundaryScore);
            }

            // All tiedCandidates should have score == boundaryScore
            for (PlayerAggregate tied : result.tiedCandidates()) {
                assertEquals(boundaryScore, tied.totalScore(),
                        "tiedCandidate has score " + tied.totalScore() + " which is not == boundary " + boundaryScore);
            }

            // Count of tied candidates should match all players at boundary score
            long expectedTied = aggregates.stream()
                    .filter(a -> a.totalScore() == boundaryScore)
                    .count();
            assertEquals(expectedTied, result.tiedCandidates().size());
        } else {
            // No tie: top 3 in definiteWinners
            assertEquals(Math.min(3, aggregates.size()), result.definiteWinners().size());
            assertTrue(result.tiedCandidates().isEmpty());
        }

        // Special case: all same score and >= 3
        int highestScore = sorted.get(0).totalScore();
        boolean allSame = sorted.stream().allMatch(a -> a.totalScore() == highestScore);
        if (allSame && sorted.size() >= 3) {
            assertTrue(result.definiteWinners().isEmpty());
            assertEquals(sorted.size(), result.tiedCandidates().size());
        }
    }
}
