package com.topthree;

/**
 * Formats a ScoreRecord back into canonical CSV format.
 * 
 * The output format matches the input format: player_id, player_name, game_id, game_name, hours_played, normalised_score
 * No leading/trailing whitespace is added around fields.
 */
public interface PrettyPrinter {
    /**
     * Formats a ScoreRecord as a six-field comma-separated string.
     * 
     * @param record the ScoreRecord to format
     * @return a CSV string in the format: player_id, player_name, game_id, game_name, hours_played, normalised_score
     */
    String print(ScoreRecord record);
}
