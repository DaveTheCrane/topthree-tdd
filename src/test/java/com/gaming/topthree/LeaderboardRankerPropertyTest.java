package com.gaming.topthree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link LeaderboardRankerImpl} (Properties 10-11).
 */
class LeaderboardRankerPropertyTest {

    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    private static PlayerAggregate player(String id, int totalScore) {
        return new PlayerAggregate(new Player(id, id + "-name"), totalScore);
    }

    // ── Property 10: distinct scores, so no tie anywhere ──────────────────

    @Provide
    Arbitrary<List<PlayerAggregate>> distinctScorePlayers() {
        Arbitrary<Set<Integer>> distinctScores =
                Arbitraries.integers().between(-10_000, 10_000).set().ofMinSize(1).ofMaxSize(10);
        return distinctScores.map(scores -> {
            List<PlayerAggregate> players = new ArrayList<>();
            int i = 0;
            for (int score : scores) {
                players.add(player("p" + i++, score));
            }
            return players;
        });
    }

    // Feature: top-three-high-scores, Property 10: No-tie ranking places top players in definite_winners
    @Property(tries = 1000)
    void noTiePlacesTopPlayersInDefiniteWinners(
            @ForAll("distinctScorePlayers") List<PlayerAggregate> players) {
        RankedResult result = ranker.rank(players);

        List<Integer> descendingScores = players.stream()
                .map(PlayerAggregate::totalScore)
                .sorted((a, b) -> Integer.compare(b, a))
                .toList();
        int expectedWinners = Math.min(3, players.size());

        assertThat(result.tiedCandidates()).isEmpty();
        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactlyElementsOf(descendingScores.subList(0, expectedWinners));
    }

    // ── Property 11: a tie that straddles the third-place boundary ────────

    /** An input list together with its expected partition. */
    record TieScenario(List<PlayerAggregate> input,
                       List<Integer> expectedWinnerScores,
                       int boundaryScore,
                       int tiedCount) {}

    @Provide
    Arbitrary<TieScenario> tieScenarios() {
        Arbitrary<Integer> boundary = Arbitraries.integers().between(200_000, 300_000);
        Arbitrary<Set<Integer>> aboveOffsets =
                Arbitraries.integers().between(1, 100_000).set().ofMinSize(0).ofMaxSize(2);
        Arbitrary<Integer> extra = Arbitraries.integers().between(0, 4);
        Arbitrary<Set<Integer>> belowOffsets =
                Arbitraries.integers().between(1, 100_000).set().ofMinSize(0).ofMaxSize(3);

        return Combinators.combine(boundary, aboveOffsets, extra, belowOffsets)
                .as((b, aboveOff, ex, belowOff) -> {
                    List<Integer> aboveScores = aboveOff.stream().map(o -> b + o).sorted().toList();
                    int aboveCount = aboveScores.size();
                    // Ensure the tie block spans positions 2 and 3 (0-indexed) so the boundary ties.
                    int tiedCount = (4 - aboveCount) + ex;
                    List<Integer> belowScores = belowOff.stream().map(o -> b - o).toList();

                    List<PlayerAggregate> input = new ArrayList<>();
                    int id = 0;
                    for (int s : aboveScores) {
                        input.add(player("w" + id++, s));
                    }
                    for (int i = 0; i < tiedCount; i++) {
                        input.add(player("t" + id++, b));
                    }
                    for (int s : belowScores) {
                        input.add(player("x" + id++, s));
                    }

                    List<Integer> expectedWinnerScores = new ArrayList<>(aboveScores);
                    expectedWinnerScores.sort((x, y) -> Integer.compare(y, x)); // descending
                    return new TieScenario(input, expectedWinnerScores, b, tiedCount);
                });
    }

    // Feature: top-three-high-scores, Property 11: Tie-at-boundary produces correct partition
    @Property(tries = 1000)
    void tieAtBoundaryProducesCorrectPartition(@ForAll("tieScenarios") TieScenario scenario) {
        RankedResult result = ranker.rank(scenario.input());

        // definite_winners: exactly the players strictly above the boundary score, descending.
        assertThat(result.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactlyElementsOf(scenario.expectedWinnerScores());
        // definite_winners is empty when the boundary score equals the highest score.
        if (scenario.expectedWinnerScores().isEmpty()) {
            assertThat(result.definiteWinners()).isEmpty();
        }
        // tied_candidates: exactly the players sharing the boundary score.
        assertThat(result.tiedCandidates()).hasSize(scenario.tiedCount());
        assertThat(result.tiedCandidates())
                .allMatch(p -> p.totalScore() == scenario.boundaryScore());
    }
}
