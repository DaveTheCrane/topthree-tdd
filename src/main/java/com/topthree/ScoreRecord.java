package com.topthree;

/**
 * A parsed CSV record containing a player and a single game entry they played.
 * This is the output of CsvParser.
 */
public record ScoreRecord(Player player, GameEntry gameEntry) {}
