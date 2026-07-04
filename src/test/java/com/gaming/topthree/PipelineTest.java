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
}
