package com.topthree;

import java.util.List;

/**
 * Parses CSV lines into structured ScoreRecord objects.
 * 
 * Expected CSV format: player_id, player_name, game_id, game_name, hours_played, normalised_score
 * - Exactly six comma-separated fields required
 * - Fields are trimmed before validation
 * - player_id and game_id must be non-empty after trimming
 * - hours_played must be a valid integer
 * - normalised_score must be an integer in the range [1, 100]
 */
public interface CsvParser {
    /**
     * Parses a single CSV line into a ScoreRecord.
     * 
     * @param csvLine the CSV line to parse
     * @return Ok(ScoreRecord) if parsing succeeds, or Err(ParseError) if the line is malformed
     */
    Result<ScoreRecord, ParseError> parseLine(String csvLine);

    /**
     * Parses an ordered list of CSV lines into ScoreRecords.
     * 
     * @param csvLines the list of CSV lines to parse
     * @return Ok(List<ScoreRecord>) containing all parsed records in order, or Err(ParseError) 
     *         for the first invalid line encountered
     */
    Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines);
}
