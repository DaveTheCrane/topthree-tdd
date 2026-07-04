package com.topthree;

import com.topthree.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new DefaultPipeline();

    @Test
    void invalidCsvLineReturnsPipelineError() {
        var lines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "bad line with wrong fields"
        );
        var result = pipeline.run(lines);

        assertInstanceOf(Result.Err.class, result);
        var error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertEquals("bad line with wrong fields", error.context());
    }

    @Test
    void validCsvProducesCorrectRankedResult() {
        var lines = List.of(
                "p1,Alice,g1,Chess,10,80",   // 800
                "p2,Bob,g2,Go,5,60",          // 300
                "p3,Carol,g3,Tetris,8,90"     // 720
        );
        var result = pipeline.run(lines);

        assertInstanceOf(Result.Ok.class, result);
        var ranked = ((Result.Ok<RankedResult, PipelineError>) result).value();
        assertEquals(3, ranked.definiteWinners().size());
        assertEquals("p1", ranked.definiteWinners().get(0).player().playerId()); // 800
        assertEquals("p3", ranked.definiteWinners().get(1).player().playerId()); // 720
        assertEquals("p2", ranked.definiteWinners().get(2).player().playerId()); // 300
        assertTrue(ranked.tiedCandidates().isEmpty());
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        var result = pipeline.run(List.of());

        assertInstanceOf(Result.Ok.class, result);
        var ranked = ((Result.Ok<RankedResult, PipelineError>) result).value();
        assertTrue(ranked.definiteWinners().isEmpty());
        assertTrue(ranked.tiedCandidates().isEmpty());
    }
}
