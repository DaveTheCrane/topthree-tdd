package com.topthree;

/**
 * Represents a unique player identified by their id and display name.
 * Both playerId and playerName must be non-empty after trimming.
 */
public record Player(String playerId, String playerName) {}
