package com.topthree;

/**
 * Represents a player aggregated across all their game entries.
 * Contains the player info and their total weighted score.
 */
public record PlayerAggregate(Player player, int totalScore) {}
