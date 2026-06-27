package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new TopThreePipelineImpl();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());

        assertInstanceOf(Result.Ok.class, result);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        assertEquals(List.of(), ok.value().definiteWinners());
        assertEquals(List.of(), ok.value().tiedCandidates());
    }
}
