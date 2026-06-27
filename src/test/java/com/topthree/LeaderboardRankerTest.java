package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerTest {

    private LeaderboardRanker ranker;

    @BeforeEach
    void setUp() {
        ranker = new LeaderboardRankerImpl();
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        RankedResult result = ranker.rank(List.of());

        assertNotNull(result);
        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void singlePlayerGoesIntoDefiniteWinners() {
        PlayerAggregate player = new PlayerAggregate(new Player("p1", "Alice"), 100);

        RankedResult result = ranker.rank(List.of(player));

        assertEquals(List.of(player), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void threeDistinctScorePlayersAllInDefiniteWinnersDescending() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 300);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 200);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        // Pass in non-sorted order to verify sorting
        RankedResult result = ranker.rank(List.of(p2, p3, p1));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void fourPlayersNoTieTopThreeInDefiniteWinnersFourthExcluded() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 100);

        RankedResult result = ranker.rank(List.of(p4, p2, p1, p3));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void tieAtPositionThreeSplitsDefiniteWinnersAndTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 400);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 300);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 200);
        PlayerAggregate p4 = new PlayerAggregate(new Player("p4", "Diana"), 200);

        RankedResult result = ranker.rank(List.of(p3, p1, p4, p2));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertEquals(List.of(p3, p4), result.tiedCandidates());
    }

    @Test
    void allPlayersSameScoreDefiniteWinnersEmptyAllInTiedCandidates() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 100);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);
        PlayerAggregate p3 = new PlayerAggregate(new Player("p3", "Charlie"), 100);

        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(p1, p2, p3), result.tiedCandidates());
    }

    @Test
    void twoDistinctScorePlayersBothInDefiniteWinners() {
        PlayerAggregate p1 = new PlayerAggregate(new Player("p1", "Alice"), 200);
        PlayerAggregate p2 = new PlayerAggregate(new Player("p2", "Bob"), 100);

        RankedResult result = ranker.rank(List.of(p2, p1));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    // **Validates: Requirements 3.2, 3.4**
    @Property(tries = 1000)
    void noTieRankingPlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScorePlayerAggregates") List<PlayerAggregate> aggregates) {

        LeaderboardRanker ranker = new LeaderboardRankerImpl();
        RankedResult result = ranker.rank(aggregates);

        int n = aggregates.size();
        int expectedWinnerCount = Math.min(3, n);

        // Sort expected descending by totalScore
        List<PlayerAggregate> sortedDesc = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .toList();

        List<PlayerAggregate> expectedWinners = sortedDesc.subList(0, expectedWinnerCount);

        assertEquals(expectedWinners, result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScorePlayerAggregates() {
        return Arbitraries.integers().between(1, 6).flatMap(size ->
                Arbitraries.integers().between(1, 100000)
                        .set().ofSize(size)
                        .map(scores -> {
                            List<Integer> scoreList = new ArrayList<>(scores);
                            List<PlayerAggregate> players = new ArrayList<>();
                            for (int i = 0; i < scoreList.size(); i++) {
                                players.add(new PlayerAggregate(
                                        new Player("p" + (i + 1), "Player" + (i + 1)),
                                        scoreList.get(i)));
                            }
                            Collections.shuffle(players);
                            return players;
                        })
        );
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    // **Validates: Requirements 3.3, 3.6**
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(
            @ForAll("tiedAtBoundaryPlayerAggregates") List<PlayerAggregate> aggregates) {

        LeaderboardRanker ranker = new LeaderboardRankerImpl();
        RankedResult result = ranker.rank(aggregates);

        // Sort descending by totalScore
        List<PlayerAggregate> sortedDesc = aggregates.stream()
                .sorted(Comparator.comparingInt(PlayerAggregate::totalScore).reversed())
                .toList();

        int highestScore = sortedDesc.get(0).totalScore();
        boolean allSameScore = sortedDesc.stream().allMatch(p -> p.totalScore() == highestScore);

        if (allSameScore) {
            // All-tied degenerate case: definiteWinners empty, all in tiedCandidates
            assertEquals(List.of(), result.definiteWinners());
            assertEquals(sortedDesc.size(), result.tiedCandidates().size());
            assertTrue(result.tiedCandidates().containsAll(sortedDesc));
        } else {
            // Determine the boundary score (score at index 2 in 0-indexed sorted list)
            int boundaryScore = sortedDesc.get(2).totalScore();

            // Players strictly above boundary → definiteWinners
            List<PlayerAggregate> expectedDefinite = sortedDesc.stream()
                    .filter(p -> p.totalScore() > boundaryScore)
                    .toList();

            // Players at boundary score → tiedCandidates
            List<PlayerAggregate> expectedTied = sortedDesc.stream()
                    .filter(p -> p.totalScore() == boundaryScore)
                    .toList();

            assertEquals(expectedDefinite, result.definiteWinners());
            assertEquals(expectedTied.size(), result.tiedCandidates().size());
            assertTrue(result.tiedCandidates().containsAll(expectedTied));
        }
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> tiedAtBoundaryPlayerAggregates() {
        // Strategy: generate K players above boundary + M >= 2 players at boundary score
        // K < 3 so that the tied players are at the boundary position
        // Also include the all-tied case
        return Arbitraries.oneOf(
                // Case 1: All-tied (all same score, 2+ players)
                allTiedCase(),
                // Case 2: K players above boundary + M tied at boundary + optional below
                tiedAtBoundaryCase()
        );
    }

    private Arbitrary<List<PlayerAggregate>> allTiedCase() {
        return Combinators.combine(
                Arbitraries.integers().between(2, 6),
                Arbitraries.integers().between(1, 100000)
        ).as((count, score) -> {
            List<PlayerAggregate> players = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                players.add(new PlayerAggregate(
                        new Player("p" + (i + 1), "Player" + (i + 1)),
                        score));
            }
            Collections.shuffle(players);
            return players;
        });
    }

    private Arbitrary<List<PlayerAggregate>> tiedAtBoundaryCase() {
        // For a valid boundary tie: need N >= 4 and sorted[2].score == sorted[3].score
        // K = players strictly above boundary (0, 1, or 2)
        // M = players at boundary score (must ensure K + M >= 4, i.e. tied players occupy positions 2 and 3+)
        // B = players below boundary (0+)
        return Arbitraries.integers().between(0, 2).flatMap(k -> {
            // M must be large enough that tied players reach position 3
            // Positions 0..K-1 are above, K..K+M-1 are at boundary
            // We need K + M >= 4, so M >= 4 - K
            int minM = Math.max(2, 4 - k);
            return Combinators.combine(
                    Arbitraries.integers().between(minM, Math.max(minM, 5)),
                    Arbitraries.integers().between(0, 2),
                    Arbitraries.integers().between(100, 50000),
                    Arbitraries.integers().between(1, 99)
            ).as((m, b, boundaryScore, gap) -> {
                List<PlayerAggregate> players = new ArrayList<>();
                int playerIdx = 1;

                // K players above boundary with distinct scores above
                for (int i = 0; i < k; i++) {
                    int aboveScore = boundaryScore + gap * (k - i);
                    players.add(new PlayerAggregate(
                            new Player("p" + playerIdx, "Player" + playerIdx),
                            aboveScore));
                    playerIdx++;
                }

                // M players at boundary score
                for (int i = 0; i < m; i++) {
                    players.add(new PlayerAggregate(
                            new Player("p" + playerIdx, "Player" + playerIdx),
                            boundaryScore));
                    playerIdx++;
                }

                // B players below boundary with distinct scores below
                for (int i = 0; i < b; i++) {
                    int belowScore = boundaryScore - gap * (i + 1);
                    if (belowScore < 1) belowScore = 1;
                    players.add(new PlayerAggregate(
                            new Player("p" + playerIdx, "Player" + playerIdx),
                            belowScore));
                    playerIdx++;
                }

                Collections.shuffle(players);
                return players;
            });
        });
    }
}
