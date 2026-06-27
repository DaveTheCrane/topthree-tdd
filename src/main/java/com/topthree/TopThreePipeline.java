package com.topthree;

import java.util.List;

public interface TopThreePipeline {
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
