package com.topthree;

import net.jqwik.api.*;
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
}
