package com.topthree;

import com.topthree.model.PlayerAggregate;
import com.topthree.model.RankedResult;

import java.util.List;

public interface LeaderboardRanker {

    /**
     * Ranks players by totalScore descending and returns a RankedResult.
     * definiteWinners: players unambiguously in the top three.
     * tiedCandidates: players sharing the boundary score that creates ambiguity.
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
