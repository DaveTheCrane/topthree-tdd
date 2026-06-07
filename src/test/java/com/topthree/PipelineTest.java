package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new DefaultTopThreePipeline();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());

        assertInstanceOf(Result.Ok.class, result);

        RankedResult ranked = ((Result.Ok<RankedResult, PipelineError>) result).value();
        assertEquals(List.of(), ranked.definiteWinners());
        assertEquals(List.of(), ranked.tiedCandidates());
    }
}
