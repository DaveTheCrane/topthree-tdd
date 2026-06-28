package com.topthree;

/**
 * A parsed, structured representation of one CSV_Record, containing a Player and a Game_Entry.
 */
public record ScoreRecord(Player player, GameEntry gameEntry) {}
