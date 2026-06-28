package com.topthree;

/**
 * The combined result for one Player across all Game_Entry rows: total weighted score used for ranking.
 */
public record PlayerAggregate(Player player, int totalScore) {}
