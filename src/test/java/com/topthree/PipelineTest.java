package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineTest {

    private final TopThreePipeline pipeline = new DefaultPipeline();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        assertThat(ok.value().definiteWinners()).isEmpty();
        assertThat(ok.value().tiedCandidates()).isEmpty();
    }
}
