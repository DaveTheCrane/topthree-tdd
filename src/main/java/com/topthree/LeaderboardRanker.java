package com.topthree;

import java.util.List;

/**
 * Ranks players by total score and returns a RankedResult with tie-boundary handling.
 * 
 * Produces two lists:
 * - definiteWinners: players unambiguously in the top three (sorted descending by totalScore)
 * - tiedCandidates: players sharing the boundary score that creates ambiguity at a top-three position
 * 
 * Boundary logic:
 * - If fewer than three players and no tie: all go to definiteWinners
 * - If three or more players with no tie at boundary: top three go to definiteWinners
 * - If tie at boundary position: all strictly above boundary go to definiteWinners,
 *   all at boundary score go to tiedCandidates
 * - If all players share the same score: definiteWinners is empty, all go to tiedCandidates
 */
public interface LeaderboardRanker {
    /**
     * Ranks players by total score descending and returns a RankedResult.
     * 
     * @param aggregates the list of PlayerAggregate objects to rank
     * @return a RankedResult with definiteWinners and tiedCandidates populated according to tie logic
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
