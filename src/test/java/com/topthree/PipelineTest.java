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

    @Test
    void duplicatePlayerIdGameIdReturnsPipelineError() {
        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "p1,Alice,g1,Chess,5,60"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertTrue(err.error().message().contains("duplicate") || err.error().message().contains("Duplicate"));
    }

    @Test
    void invalidCsvLineReturnsPipelineError() {
        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "this,is,malformed"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertNotNull(err.error().message());
        assertEquals("this,is,malformed", err.error().context());
    }

    @Test
    void validCsvListProducesCorrectRankedResult() {
        // p1: 10*80 = 800, p2: 5*60 = 300, p3: 3*90 = 270
        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,10,80",
                "p2,Bob,g2,Go,5,60",
                "p3,Charlie,g3,Poker,3,90"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Ok.class, result);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        RankedResult ranked = ok.value();

        assertEquals(3, ranked.definiteWinners().size());
        assertEquals(List.of(), ranked.tiedCandidates());

        // Descending order: Alice(800), Bob(300), Charlie(270)
        assertEquals("p1", ranked.definiteWinners().get(0).player().playerId());
        assertEquals(800, ranked.definiteWinners().get(0).totalScore());
        assertEquals("p2", ranked.definiteWinners().get(1).player().playerId());
        assertEquals(300, ranked.definiteWinners().get(1).totalScore());
        assertEquals("p3", ranked.definiteWinners().get(2).player().playerId());
        assertEquals(270, ranked.definiteWinners().get(2).totalScore());
    }
}
