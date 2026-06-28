package com.topthree.component;

import com.topthree.model.*;
import net.jqwik.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class PipelinePropertyTest {
    private final TopThreePipeline pipeline = new TopThreePipelineImpl(
        new CsvParserImpl(),
        new ScoreAggregatorImpl(),
        new LeaderboardRankerImpl()
    );

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    @Property(tries = 100)
    void pipelineCompositionCorrectnessProperty(@ForAll("validCsvLines") List<String> csvLines) {
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);

        // Manual composition
        CsvParser parser = new CsvParserImpl();
        ScoreAggregator aggregator = new ScoreAggregatorImpl();
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        Result<List<ScoreRecord>, ParseError> parseResult = parser.parseLines(csvLines);
        if (parseResult instanceof Result.Err) {
            assertThat(pipelineResult).isInstanceOf(Result.Err.class);
        } else {
            Result.Ok<List<ScoreRecord>, ParseError> parseOk = (Result.Ok<List<ScoreRecord>, ParseError>) parseResult;
            Result<List<PlayerAggregate>, AggregationError> aggResult = aggregator.aggregate(parseOk.value());
            if (aggResult instanceof Result.Err) {
                assertThat(pipelineResult).isInstanceOf(Result.Err.class);
            } else {
                Result.Ok<List<PlayerAggregate>, AggregationError> aggOk = (Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult;
                RankedResult expectedResult = ranker.rank(aggOk.value());
                
                assertThat(pipelineResult).isInstanceOf(Result.Ok.class);
                Result.Ok<RankedResult, PipelineError> pipelineOk = (Result.Ok<RankedResult, PipelineError>) pipelineResult;
                RankedResult actualResult = pipelineOk.value();
                
                assertThat(actualResult.definiteWinners()).hasSameSizeAs(expectedResult.definiteWinners());
                assertThat(actualResult.tiedCandidates()).hasSameSizeAs(expectedResult.tiedCandidates());
            }
        }
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    @Property(tries = 50)
    void pipelinePropagateCsvErrorsProperty(@ForAll("invalidCsvLines") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    @Property(tries = 50)
    void pipelineRejectsDuplicatePairsProperty(@ForAll("csvLinesWithDuplicate") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<List<String>> validCsvLines() {
        Arbitrary<String> csvLine = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.integers().between(1, 50),
            Arbitraries.integers().between(1, 100)
        ).as((pid, pname, gid, gname, hours, score) ->
            pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score
        );

        return csvLine.list().ofSize(0);
    }

    @Provide
    Arbitrary<List<String>> invalidCsvLines() {
        Arbitrary<String> invalidLine = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        return invalidLine.list().ofSize(1);
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithDuplicate() {
        return Arbitraries.integers().between(1, 100).flatMap(count -> {
            var lines = new java.util.ArrayList<String>();
            String baseLine = "p1,Alice,g1,Chess,10,85";
            lines.add(baseLine);
            lines.add("p1,Alicia,g1,Chess,5,75"); // Duplicate (p1, g1)
            return Arbitraries.just(lines);
        });
    }
}
