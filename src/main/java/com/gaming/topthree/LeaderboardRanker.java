package com.gaming.topthree;

import java.util.List;

/**
 * Sorts {@link PlayerAggregate} objects and returns a {@link RankedResult} that
 * surfaces tie ambiguity at boundary positions.
 */
public interface LeaderboardRanker {

    /**
     * Ranks players by totalScore descending and returns a RankedResult.
     * definiteWinners: players unambiguously in the top three.
     * tiedCandidates: players sharing the boundary score that creates ambiguity.
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
