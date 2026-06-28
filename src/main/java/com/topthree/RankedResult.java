package com.topthree;

import java.util.List;

/**
 * The result of ranking players for the top three positions.
 * Surfaces tie ambiguity at boundary positions.
 *
 * @param definiteWinners   Players unambiguously in the top three
 * @param tiedCandidates    Players sharing the boundary score (empty when no tie)
 */
public record RankedResult(List<PlayerAggregate> definiteWinners, List<PlayerAggregate> tiedCandidates) {}
