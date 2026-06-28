package com.topthree.component;

import com.topthree.model.PlayerAggregate;
import com.topthree.model.RankedResult;

import java.util.List;

public interface LeaderboardRanker {
    /**
     * Ranks players by Total_Score descending and returns a RankedResult.
     * definite_winners: players unambiguously in the top three.
     * tied_candidates: players sharing the boundary score that creates ambiguity.
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
