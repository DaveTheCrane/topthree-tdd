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

    @Test
    void validCsvListProducesCorrectRankedResult() {
        // p1: 10 * 80 = 800, p2: 5 * 60 = 300, p3: 3 * 90 = 270
        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "p2,Bob,g2,Poker,5,60",
                "p3,Carol,g3,Tennis,3,90"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Ok.class, result);

        RankedResult ranked = ((Result.Ok<RankedResult, PipelineError>) result).value();
        assertEquals(3, ranked.definiteWinners().size());
        assertEquals(List.of(), ranked.tiedCandidates());

        // Verify order: Alice (800), Bob (300), Carol (270)
        assertEquals("p1", ranked.definiteWinners().get(0).player().playerId());
        assertEquals(800, ranked.definiteWinners().get(0).totalScore());
        assertEquals("p2", ranked.definiteWinners().get(1).player().playerId());
        assertEquals(300, ranked.definiteWinners().get(1).totalScore());
        assertEquals("p3", ranked.definiteWinners().get(2).player().playerId());
        assertEquals(270, ranked.definiteWinners().get(2).totalScore());
    }

    @Test
    void invalidCsvLineReturnsPipelineError() {
        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "this is not valid csv"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);

        PipelineError error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertNotNull(error.message());
        assertEquals("this is not valid csv", error.context());
    }
}
