package com.topthree;

import java.util.List;

/**
 * Wires all three components (CsvParser, ScoreAggregator, LeaderboardRanker) end-to-end.
 */
public interface TopThreePipeline {

    /**
     * Runs the full pipeline: parse -> aggregate -> rank.
     * Returns PipelineError on any invalid CSV line, duplicate player/game pair,
     * or aggregation error. Returns RankedResult on success.
     */
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
