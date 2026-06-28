package com.topthree;

import java.util.List;

/**
 * End-to-end pipeline that chains all three components: CsvParser → ScoreAggregator → LeaderboardRanker.
 * 
 * Orchestrates the full processing flow from raw CSV strings to a ranked result.
 * Also handles duplicate detection: if the same (playerId, gameId) combination appears more than once,
 * returns a PipelineError.
 */
public interface TopThreePipeline {
    /**
     * Runs the full pipeline: parse CSV → aggregate scores → rank players.
     * 
     * @param csvLines the list of raw CSV strings
     * @return Ok(RankedResult) containing the top three players (or fewer if fewer players exist),
     *         or Err(PipelineError) if any step fails (parse error, aggregation error, duplicate pair)
     */
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
