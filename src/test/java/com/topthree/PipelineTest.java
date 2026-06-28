package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new TopThreePipelineImpl();

    // Feature: top-three-high-scores, Requirement 4.4: Empty input returns empty RankedResult
    @Test
    void run_returnsEmptyRankedResult_forEmptyInput() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());
        
        assertTrue(result.isOk());
        RankedResult rankedResult = result.get();
        assertEquals(List.of(), rankedResult.definiteWinners());
        assertEquals(List.of(), rankedResult.tiedCandidates());
    }

    // Feature: top-three-high-scores, Requirement 4.1: Valid CSV list produces correct RankedResult
    @Test
    void run_validCSV_producesCorrectRankedResult() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p2,Bob,g2,Checkers,3,60"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertTrue(result.isOk());
        RankedResult rankedResult = result.get();
        assertEquals(2, rankedResult.definiteWinners().size());
        assertEquals("p2", rankedResult.definiteWinners().get(0).player().playerId());
        assertEquals("p1", rankedResult.definiteWinners().get(1).player().playerId());
    }

    // Feature: top-three-high-scores, Requirement 4.2: Invalid CSV line returns PipelineError
    @Test
    void run_invalidCSV_returnsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,abc,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertTrue(result.isErr());
        PipelineError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("error"));
    }

    // Feature: top-three-high-scores, Requirement 4.3: Duplicate playerId/gameId returns PipelineError
    @Test
    void run_duplicatePlayerGame_returnsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p1,Alice,g1,Chess,3,60"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertTrue(result.isErr());
        PipelineError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("duplicate"));
    }

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    @Test
    void run_validCSV_matchesManualChaining() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p2,Bob,g2,Checkers,3,60"
        );
        
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);
        
        // Manually chain the components
        Result<List<ScoreRecord>, ParseError> parseResult = new CsvParserImpl().parseLines(csvLines);
        Result<List<PlayerAggregate>, AggregationError> aggregateResult = new ScoreAggregatorImpl().aggregate(parseResult.get());
        RankedResult expected = new LeaderboardRankerImpl().rank(aggregateResult.get());
        
        assertTrue(pipelineResult.isOk());
        RankedResult actual = pipelineResult.get();
        assertEquals(expected.definiteWinners(), actual.definiteWinners());
        assertEquals(expected.tiedCandidates(), actual.tiedCandidates());
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    @Test
    void run_csvParseError_propagatedAsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "invalid,csv,line"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertTrue(result.isErr());
        PipelineError error = result.getError();
        assertTrue(error.message().toLowerCase().contains("error") || error.context().toLowerCase().contains("parse"));
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate playerId/gameId pairs
    @Test
    void run_duplicatePlayerGame_propagatedAsPipelineError() {
        List<String> csvLines = List.of(
            "p1,Alice,g1,Chess,2,50",
            "p1,Alice,g1,Chess,3,60"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertTrue(result.isErr());
        PipelineError error = result.getError();
        assertEquals("duplicate", error.context());
    }
}
