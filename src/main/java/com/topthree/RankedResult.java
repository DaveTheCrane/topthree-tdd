package com.topthree;

import java.util.List;

/**
 * The output of the Leaderboard_Ranker, containing two fields:
 * - definiteWinners: players unambiguously in the top three
 * - tiedCandidates: players sharing the boundary score that creates tie ambiguity
 */
public record RankedResult(
    List<PlayerAggregate> definiteWinners,
    List<PlayerAggregate> tiedCandidates
) {}
