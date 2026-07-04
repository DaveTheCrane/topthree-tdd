package com.gaming.topthree;

import java.util.List;

/**
 * The output of the LeaderboardRanker.
 *
 * @param definiteWinners players unambiguously in the top three
 * @param tiedCandidates  players sharing the boundary score; empty when no tie exists
 */
public record RankedResult(
        List<PlayerAggregate> definiteWinners,
        List<PlayerAggregate> tiedCandidates
) {}
