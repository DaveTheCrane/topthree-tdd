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

    @Test
    void invalidCsvLineReturnsPipelineError() {
        List<String> lines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "invalid"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(lines);

        assertThat(result).isInstanceOf(Result.Err.class);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertThat(err.error()).isInstanceOf(PipelineError.class);
    }

    @Test
    void duplicatePlayerIdGameIdPairReturnsPipelineError() {
        List<String> lines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p1,Alice,g1,Chess,3,80"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(lines);

        assertThat(result).isInstanceOf(Result.Err.class);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertThat(err.error()).isInstanceOf(PipelineError.class);
    }

    @Test
    void validCsvListProducesCorrectRankedResultEndToEnd() {
        List<String> lines = List.of(
            "p1,Alice,g1,Chess,10,80",   // weighted score = 800
            "p2,Bob,g2,Go,5,60",         // weighted score = 300
            "p3,Charlie,g3,Poker,3,50"   // weighted score = 150
        );

        Result<RankedResult, PipelineError> result = pipeline.run(lines);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        RankedResult ranked = ok.value();

        assertThat(ranked.tiedCandidates()).isEmpty();
        assertThat(ranked.definiteWinners()).hasSize(3);

        // Verify descending order by totalScore
        assertThat(ranked.definiteWinners().get(0)).isEqualTo(
            new PlayerAggregate(new Player("p1", "Alice"), 800));
        assertThat(ranked.definiteWinners().get(1)).isEqualTo(
            new PlayerAggregate(new Player("p2", "Bob"), 300));
        assertThat(ranked.definiteWinners().get(2)).isEqualTo(
            new PlayerAggregate(new Player("p3", "Charlie"), 150));
    }
}
