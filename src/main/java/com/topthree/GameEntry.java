package com.topthree;

/**
 * Represents a single game entry: a game played by a player with
 * a number of hours played and a normalised score (1–100).
 */
public record GameEntry(String gameId, String gameName, int hoursPlayed, int normalisedScore) {}
