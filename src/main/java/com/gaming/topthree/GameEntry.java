package com.gaming.topthree;

/**
 * A single game played by a player in the current week, with hours played and a
 * normalised score (integer 1-100).
 */
public record GameEntry(String gameId, String gameName, int hoursPlayed, int normalisedScore) {}
