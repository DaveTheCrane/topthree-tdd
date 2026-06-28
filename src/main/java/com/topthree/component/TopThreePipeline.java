package com.topthree.component;

import com.topthree.model.PipelineError;
import com.topthree.model.RankedResult;
import com.topthree.model.Result;

import java.util.List;

public interface TopThreePipeline {
    /**
     * Runs the full pipeline: parse → aggregate → rank.
     * Returns PipelineError on any invalid CSV line, duplicate player/game pair,
     * or aggregation error. Returns RankedResult on success.
     */
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
