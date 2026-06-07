package com.topthree;

import net.jqwik.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardRankerPropertyTest {

    private final LeaderboardRanker ranker = new DefaultLeaderboardRanker();

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    // **Validates: Requirements 3.2, 3.4**
    @Property(tries = 1000)
    void noTieRankingPlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScoreAggregates") List<PlayerAggregate> aggregates
    ) {
        RankedResult result = ranker.rank(aggregates);

        int expectedCount = Math.min(3, aggregates.size());

        // definiteWinners should contain exactly min(3, N) players
        assertEquals(expectedCount, result.definiteWinners().size(),
                "definiteWinners should have min(3, N) players");

        // tiedCandidates should be empty
        assertTrue(result.tiedCandidates().isEmpty(),
                "tiedCandidates should be empty when no ties exist");

        // definiteWinners should be in descending order of totalScore
        List<PlayerAggregate> winners = result.definiteWinners();
        for (int i = 0; i < winners.size() - 1; i++) {
            assertTrue(winners.get(i).totalScore() > winners.get(i + 1).totalScore(),
                    "definiteWinners must be in strict descending order of totalScore");
        }

        // definiteWinners should contain the top min(3, N) players by score
        List<PlayerAggregate> sortedInput = new ArrayList<>(aggregates);
        sortedInput.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());
        List<PlayerAggregate> expectedWinners = sortedInput.subList(0, expectedCount);
        assertEquals(expectedWinners, winners,
                "definiteWinners should be the top min(3, N) players in descending order");
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScoreAggregates() {
        // Generate 1-6 players with all-distinct totalScores
        return Arbitraries.integers().between(1, 6).flatMap(count ->
                Arbitraries.integers().between(1, 10000)
                        .list().ofSize(count).uniqueElements()
                        .map(scores -> {
                            List<PlayerAggregate> aggregates = new ArrayList<>();
                            for (int i = 0; i < scores.size(); i++) {
                                String pid = "p" + (i + 1);
                                String name = "Player" + (i + 1);
                                aggregates.add(new PlayerAggregate(new Player(pid, name), scores.get(i)));
                            }
                            return aggregates;
                        })
        );
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    // **Validates: Requirements 3.3, 3.6**
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(
            @ForAll("aggregatesWithTieAtBoundary") List<PlayerAggregate> aggregates
    ) {
        RankedResult result = ranker.rank(aggregates);

        // Sort input descending to determine the boundary score
        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());

        int boundaryScore = sorted.get(Math.min(2, sorted.size() - 1)).totalScore();

        // definiteWinners should contain only players with score > boundaryScore
        for (PlayerAggregate w : result.definiteWinners()) {
            assertTrue(w.totalScore() > boundaryScore,
                    "All definiteWinners must have score strictly above boundary score " + boundaryScore);
        }

        // tiedCandidates should contain all players with score == boundaryScore
        List<PlayerAggregate> expectedTied = sorted.stream()
                .filter(p -> p.totalScore() == boundaryScore)
                .toList();
        assertEquals(expectedTied.size(), result.tiedCandidates().size(),
                "tiedCandidates should contain all players at boundary score " + boundaryScore);
        assertTrue(result.tiedCandidates().containsAll(expectedTied),
                "tiedCandidates must contain all players sharing the boundary score");

        // If boundary score equals the highest score, definiteWinners should be empty
        if (boundaryScore == sorted.get(0).totalScore()) {
            assertTrue(result.definiteWinners().isEmpty(),
                    "definiteWinners must be empty when boundary score equals the highest score");
        }

        // definiteWinners should be in descending order
        List<PlayerAggregate> winners = result.definiteWinners();
        for (int i = 0; i < winners.size() - 1; i++) {
            assertTrue(winners.get(i).totalScore() > winners.get(i + 1).totalScore(),
                    "definiteWinners must be in strict descending order");
        }
    }

    @Provide
    Arbitrary<List<PlayerAggregate>> aggregatesWithTieAtBoundary() {
        // Generate lists where at least 2 players share the boundary score
        return Arbitraries.integers().between(2, 6).flatMap(count ->
                Arbitraries.integers().between(1, 10000).flatMap(boundaryScore ->
                        Arbitraries.integers().between(2, Math.min(count, 4)).flatMap(tiedCount -> {
                            // Generate some players above the boundary
                            int aboveCount = Math.min(count - tiedCount, 2);
                            List<Arbitrary<PlayerAggregate>> playerArbs = new ArrayList<>();

                            // Players above the boundary with distinct scores
                            for (int i = 0; i < aboveCount; i++) {
                                int finalI = i;
                                playerArbs.add(Arbitraries.just(
                                        new PlayerAggregate(
                                                new Player("above" + i, "Above" + i),
                                                boundaryScore + 1 + finalI * 100
                                        )
                                ));
                            }

                            // Tied players at the boundary score
                            for (int i = 0; i < tiedCount; i++) {
                                playerArbs.add(Arbitraries.just(
                                        new PlayerAggregate(
                                                new Player("tied" + i, "Tied" + i),
                                                boundaryScore
                                        )
                                ));
                            }

                            // Combine all players into a list
                            if (playerArbs.isEmpty()) {
                                return Arbitraries.just(List.<PlayerAggregate>of());
                            }
                            return combinePlayerArbs(playerArbs);
                        })
                )
        );
    }

    private Arbitrary<List<PlayerAggregate>> combinePlayerArbs(List<Arbitrary<PlayerAggregate>> arbs) {
        if (arbs.size() == 1) {
            return arbs.get(0).map(List::of);
        }
        Arbitrary<List<PlayerAggregate>> combined = arbs.get(0).map(p -> {
            List<PlayerAggregate> list = new ArrayList<>();
            list.add(p);
            return list;
        });
        for (int i = 1; i < arbs.size(); i++) {
            combined = Combinators.combine(combined, arbs.get(i)).as((list, p) -> {
                List<PlayerAggregate> newList = new ArrayList<>(list);
                newList.add(p);
                return newList;
            });
        }
        return combined;
    }
}
