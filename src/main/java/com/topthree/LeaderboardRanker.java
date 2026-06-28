package com.topthree;

import java.util.List;

/**
 * Sorts Player_Aggregate objects and returns a RankedResult that surfaces tie ambiguity at boundary positions.
 */
public interface LeaderboardRanker {

    /**
     * Ranks players by Total_Score descending and returns a RankedResult.
     * definite_winners: players unambiguously in the top three.
     * tied_candidates: players sharing the boundary score that creates ambiguity.
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
