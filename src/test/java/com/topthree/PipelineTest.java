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

    @Test
    void invalidCsvLineReturnsPipelineError() {
        var csvLines = List.of("p1,Alice,g1");

        var result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        var err = (Result.Err<RankedResult, PipelineError>) result;
        assertNotNull(err.error());
        assertInstanceOf(PipelineError.class, err.error());
    }

    @Test
    void duplicatePlayerIdGameIdReturnsPipelineError() {
        var csvLines = List.of(
            "p1,Alice,g1,Chess,10,50",
            "p1,Alice,g1,Chess,5,80"
        );

        var result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        var err = (Result.Err<RankedResult, PipelineError>) result;
        assertInstanceOf(PipelineError.class, err.error());
    }

    @Test
    void validCsvListProducesCorrectRankedResult() {
        var csvLines = List.of(
            "p1,Alice,g1,Chess,10,50",
            "p2,Bob,g2,Go,5,80",
            "p3,Charlie,g3,Poker,8,30"
        );

        var result = pipeline.run(csvLines);

        var expected = new Result.Ok<RankedResult, PipelineError>(new RankedResult(
            List.of(
                new PlayerAggregate(new Player("p1", "Alice"), 500),
                new PlayerAggregate(new Player("p2", "Bob"), 400),
                new PlayerAggregate(new Player("p3", "Charlie"), 240)
            ),
            List.of()
        ));

        assertEquals(expected, result);
    }
}
