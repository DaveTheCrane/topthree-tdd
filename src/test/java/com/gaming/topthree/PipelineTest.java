package com.gaming.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineTest {

    private final TopThreePipeline pipeline = new TopThreePipelineImpl(
            new CsvParserImpl(), new ScoreAggregatorImpl(), new LeaderboardRankerImpl());

    @SuppressWarnings("unchecked")
    private static RankedResult unwrap(Result<RankedResult, PipelineError> result) {
        assertThat(result).isInstanceOf(Result.Ok.class);
        return ((Result.Ok<RankedResult, PipelineError>) result).value();
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());

        RankedResult ranked = unwrap(result);
        assertThat(ranked.definiteWinners()).isEmpty();
        assertThat(ranked.tiedCandidates()).isEmpty();
    }

    @Test
    void validCsvProducesCorrectRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of(
                "p1,Alice,g1,Chess,2,50",   // 100
                "p2,Bob,g2,Go,3,40",        // 120
                "p3,Carol,g3,Pool,1,30"));  // 30

        RankedResult ranked = unwrap(result);
        assertThat(ranked.definiteWinners()).extracting(a -> a.player().playerId())
                .containsExactly("p2", "p1", "p3");
        assertThat(ranked.definiteWinners()).extracting(PlayerAggregate::totalScore)
                .containsExactly(120, 100, 30);
        assertThat(ranked.tiedCandidates()).isEmpty();
    }

    @Test
    void invalidCsvLineReturnsPipelineError() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of(
                "p1,Alice,g1,Chess,2,50",
                "p2,Bob,g2,Go,3"));   // wrong field count

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void duplicatePlayerGamePairReturnsPipelineError() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of(
                "p1,Alice,g1,Chess,2,50",
                "p1,Alice,g1,Chess,3,40"));   // same (playerId, gameId)

        assertThat(result).isInstanceOf(Result.Err.class);
    }
}
