package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.Combinators;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = ranker.rank(List.of());

        assertEquals(new RankedResult(List.of(), List.of()), result);
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinnersWithEmptyTiedCandidates() {
        var agg = new PlayerAggregate(new Player("p1", "Alice"), 100);

        var result = ranker.rank(List.of(agg));

        assertEquals(new RankedResult(List.of(agg), List.of()), result);
    }

    @Test
    void threeDistinctScoresAllInDefiniteWinnersDescending() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 300);

        var result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(new RankedResult(List.of(p3, p1, p2), List.of()), result);
    }

    @Test
    void fourPlayersNoTieTopThreeInDefiniteWinnersFourthExcluded() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        var p4 = new PlayerAggregate(new Player("p4", "Diana"), 100);

        var result = ranker.rank(List.of(p4, p2, p1, p3));

        assertEquals(new RankedResult(List.of(p1, p2, p3), List.of()), result);
    }

    @Test
    void allPlayersShareSameScoreAllInTiedCandidates() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        var result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(), result.definiteWinners());
        assertTrue(result.tiedCandidates().containsAll(List.of(p1, p2, p3)));
        assertEquals(3, result.tiedCandidates().size());
    }

    @Test
    void twoPlayersWithDistinctScoresBothInDefiniteWinners() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);

        var result = ranker.rank(List.of(p1, p2));

        assertEquals(new RankedResult(List.of(p1, p2), List.of()), result);
    }

    @Test
    void tieAtPosition3SplitsDefiniteWinnersAndTiedCandidates() {
        var p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        var p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        var p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        var p4 = new PlayerAggregate(new Player("p4", "Diana"), 200);

        var result = ranker.rank(List.of(p1, p2, p3, p4));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertTrue(result.tiedCandidates().containsAll(List.of(p3, p4)));
        assertEquals(2, result.tiedCandidates().size());
    }

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    // **Validates: Requirements 3.2, 3.4**
    @Property(tries = 1000)
    void noTieRankingPlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScoreAggregates") List<PlayerAggregate> aggregates) {
        int n = aggregates.size();

        var result = ranker.rank(aggregates);

        // definiteWinners has exactly min(3, N) elements
        int expectedSize = Math.min(3, n);
        assertEquals(expectedSize, result.definiteWinners().size(),
                "definiteWinners should have min(3, N) elements");

        // definiteWinners is in descending totalScore order
        for (int i = 0; i < result.definiteWinners().size() - 1; i++) {
            assertTrue(
                    result.definiteWinners().get(i).totalScore() > result.definiteWinners().get(i + 1).totalScore(),
                    "definiteWinners must be in strictly descending totalScore order");
        }

        // definiteWinners contains the top-scoring players
        List<PlayerAggregate> sortedByScore = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());
        List<PlayerAggregate> expectedTopPlayers = sortedByScore.subList(0, expectedSize);
        assertEquals(expectedTopPlayers, result.definiteWinners(),
                "definiteWinners should contain the top-scoring players in descending order");

        // tiedCandidates is empty
        assertTrue(result.tiedCandidates().isEmpty(),
                "tiedCandidates should be empty when all scores are distinct");
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScoreAggregates() {
        return Arbitraries.integers().between(1, 10000)
                .set().ofMinSize(1).ofMaxSize(6)
                .map(scoreSet -> {
                    List<Integer> scores = new ArrayList<>(scoreSet);
                    Collections.shuffle(scores);
                    return IntStream.range(0, scores.size())
                            .mapToObj(i -> new PlayerAggregate(
                                    new Player("p" + i, "Player" + i),
                                    scores.get(i)))
                            .collect(Collectors.toList());
                });
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    // **Validates: Requirements 3.3, 3.6**
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(
            @ForAll("tieAtBoundaryAggregates") List<PlayerAggregate> aggregates) {

        var result = ranker.rank(aggregates);

        // Determine the boundary score: the score shared by at least two players
        // that sits at the boundary position
        List<PlayerAggregate> sorted = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .collect(Collectors.toList());

        // Check if all players share the same score (degenerate case)
        boolean allSame = sorted.stream()
                .allMatch(p -> p.totalScore() == sorted.get(0).totalScore());

        if (allSame) {
            // Degenerate case: definiteWinners empty, all in tiedCandidates
            assertTrue(result.definiteWinners().isEmpty(),
                    "When all players share the same score, definiteWinners must be empty");
            assertEquals(aggregates.size(), result.tiedCandidates().size(),
                    "When all players share the same score, all must be in tiedCandidates");
            assertTrue(result.tiedCandidates().containsAll(aggregates),
                    "tiedCandidates must contain all players when all scores are the same");
        } else {
            // Non-degenerate tie-at-boundary case (N > 3 guaranteed by generator)
            int boundaryScore = sorted.get(2).totalScore();

            // definiteWinners contains only players with score STRICTLY above the boundary
            for (PlayerAggregate winner : result.definiteWinners()) {
                assertTrue(winner.totalScore() > boundaryScore,
                        "definiteWinners must only contain players with score strictly above boundary ("
                                + boundaryScore + "), but found " + winner.totalScore());
            }

            // All players above boundary should be in definiteWinners
            List<PlayerAggregate> expectedWinners = aggregates.stream()
                    .filter(p -> p.totalScore() > boundaryScore)
                    .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedWinners.size(), result.definiteWinners().size(),
                    "definiteWinners must contain all players with score above boundary");

            // tiedCandidates contains ALL players with score EQUAL to the boundary
            List<PlayerAggregate> expectedTied = aggregates.stream()
                    .filter(p -> p.totalScore() == boundaryScore)
                    .collect(Collectors.toList());
            assertEquals(expectedTied.size(), result.tiedCandidates().size(),
                    "tiedCandidates must contain all players sharing the boundary score");
            assertTrue(result.tiedCandidates().containsAll(expectedTied),
                    "tiedCandidates must contain all players with the boundary score");

            // definiteWinners is in descending order
            for (int i = 0; i < result.definiteWinners().size() - 1; i++) {
                assertTrue(
                        result.definiteWinners().get(i).totalScore() > result.definiteWinners().get(i + 1).totalScore(),
                        "definiteWinners must be in strictly descending totalScore order");
            }
        }
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> tieAtBoundaryAggregates() {
        // Two cases:
        // 1. Degenerate: all players share the same score (aboveCount = 0)
        // 2. Non-degenerate: aboveCount in {1,2}, tiedCount ensures total > 3
        //    so that position 3 (index 2) ties with position 4+ (index 3+)
        Arbitrary<List<PlayerAggregate>> degenerateCase = Combinators.combine(
                Arbitraries.integers().between(2, 5),       // number of players
                Arbitraries.integers().between(1, 10000)    // shared score
        ).as((count, score) -> {
            List<PlayerAggregate> players = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                players.add(new PlayerAggregate(
                        new Player("p" + i, "Player" + i), score));
            }
            return players;
        });

        Arbitrary<List<PlayerAggregate>> nonDegenerateCase = Combinators.combine(
                Arbitraries.integers().between(1, 2),       // number of "above" players
                Arbitraries.integers().between(2, 3),       // number of tied players at boundary
                Arbitraries.integers().between(1, 5000)     // boundary score
        ).flatAs((aboveCount, tiedCount, boundaryScore) -> {
            // Ensure total players > 3 so tie spans position 3+
            int actualTiedCount = Math.max(tiedCount, 4 - aboveCount);

            // Generate distinct scores above boundary
            Arbitrary<List<Integer>> aboveScoresArb = Arbitraries.integers()
                    .between(boundaryScore + 1, boundaryScore + 5000)
                    .set().ofSize(aboveCount)
                    .map(ArrayList::new);

            int finalTiedCount = actualTiedCount;
            return aboveScoresArb.map(aboveScores -> {
                List<PlayerAggregate> players = new ArrayList<>();
                int idx = 0;

                // Add players above boundary
                for (int score : aboveScores) {
                    players.add(new PlayerAggregate(
                            new Player("p" + idx, "Player" + idx), score));
                    idx++;
                }

                // Add tied players at boundary score
                for (int i = 0; i < finalTiedCount; i++) {
                    players.add(new PlayerAggregate(
                            new Player("p" + idx, "Player" + idx), boundaryScore));
                    idx++;
                }

                // Shuffle to avoid input-order bias
                Collections.shuffle(players);
                return players;
            });
        });

        return Arbitraries.oneOf(degenerateCase, nonDegenerateCase);
    }
}
