package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new TopThreePipelineImpl();

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = pipeline.run(List.of());

        assertEquals(
            new Result.Ok<>(new RankedResult(List.of(), List.of())),
            result
        );
    }
}
