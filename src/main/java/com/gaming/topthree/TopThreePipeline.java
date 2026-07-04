package com.gaming.topthree;

import java.util.List;

/**
 * Wires the CSV parser, score aggregator, and leaderboard ranker end-to-end.
 */
public interface TopThreePipeline {

    /**
     * Runs the full pipeline: parse -> aggregate -> rank.
     * Returns PipelineError on any invalid CSV line, duplicate player/game pair,
     * or aggregation error. Returns RankedResult on success.
     */
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
