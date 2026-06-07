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
}
