package com.gaming.topthree;

/**
 * A parsed, structured representation of one CSV record, containing a Player and a GameEntry.
 */
public record ScoreRecord(Player player, GameEntry gameEntry) {}
