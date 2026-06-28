package com.topthree;

/**
 * Represents a single game played by a Player in the current week.
 */
public record GameEntry(String gameId, String gameName, int hoursPlayed, int normalisedScore) {}
