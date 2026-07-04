package com.gaming.topthree;

/**
 * The combined result for one player across all game entries: the total weighted
 * score used for ranking.
 */
public record PlayerAggregate(Player player, int totalScore) {}
