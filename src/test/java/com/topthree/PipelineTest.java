package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new DefaultPipeline();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = pipeline.run(List.of());

        assertInstanceOf(Result.Ok.class, result);
        var ranked = ((Result.Ok<RankedResult, PipelineError>) result).value();
        assertTrue(ranked.definiteWinners().isEmpty());
        assertTrue(ranked.tiedCandidates().isEmpty());
    }
}
