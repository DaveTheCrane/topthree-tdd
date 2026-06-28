package com.topthree.component;

import com.topthree.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class PipelineTest {
    private TopThreePipeline pipeline;

    @BeforeEach
    void setup() {
        CsvParser parser = new CsvParserImpl();
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        LeaderboardRanker ranker = new LeaderboardRankerImpl();
        pipeline = new TopThreePipelineImpl(parser, aggregator, ranker);
    }

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());
        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        RankedResult rankedResult = ok.value();
        assertThat(rankedResult.definiteWinners()).isEmpty();
        assertThat(rankedResult.tiedCandidates()).isEmpty();
    }

    @Test
    void validCsvListProducesCorrectRankedResult() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,10,85",
            "p2,Bob,g2,Checkers,5,75",
            "p3,Charlie,g3,Go,8,90"
        );
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<RankedResult, PipelineError> ok = (Result.Ok<RankedResult, PipelineError>) result;
        RankedResult rankedResult = ok.value();

        // p3: 8*90 = 720, p1: 10*85 = 850, p2: 5*75 = 375
        // Order: p1 (850), p3 (720), p2 (375)
        assertThat(rankedResult.definiteWinners()).hasSize(3);
        assertThat(rankedResult.tiedCandidates()).isEmpty();
    }

    @Test
    void invalidCsvLineReturnsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,10,85",
            "invalid"
        );
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void duplicatePlayerGamePairReturnsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,10,85",
            "p1,Alicia,g1,Chess,5,75"
        );
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertThat(result).isInstanceOf(Result.Err.class);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertThat(err.error().message()).containsIgnoringCase("duplicate");
    }
}
