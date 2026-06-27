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
}
