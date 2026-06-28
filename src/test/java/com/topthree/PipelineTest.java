package com.topthree;

import org.junit.jupiter.api.Test;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests and property-based tests for TopThreePipeline.
 */
class PipelineTest {
    private static final CsvParser csvParser = new CsvParserImpl();
    private static final ScoreAggregator scoreAggregator = new ScoreAggregatorImpl();
    private static final LeaderboardRanker leaderboardRanker = new LeaderboardRankerImpl();
    private static final TopThreePipeline pipeline = new TopThreePipelineImpl(
        csvParser, scoreAggregator, leaderboardRanker
    );

    // ─────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────

    @Test
    void testEmptyInputReturnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(new ArrayList<>());
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<RankedResult, PipelineError> ok) {
            assertThat(ok.value().definiteWinners()).isEmpty();
            assertThat(ok.value().tiedCandidates()).isEmpty();
        }
    }

    @Test
    void testValidCsvListProducesCorrectRankedResult() {
        List<String> csvLines = List.of(
            "alice,Alice,g1,Game1,10,75",
            "bob,Bob,g2,Game2,5,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<RankedResult, PipelineError> ok) {
            RankedResult ranked = ok.value();
            assertThat(ranked.definiteWinners()).hasSize(2);
            assertThat(ranked.definiteWinners().get(0).player().playerId()).isEqualTo("alice");
            assertThat(ranked.definiteWinners().get(0).totalScore()).isEqualTo(750); // 10 * 75
            assertThat(ranked.definiteWinners().get(1).player().playerId()).isEqualTo("bob");
            assertThat(ranked.definiteWinners().get(1).totalScore()).isEqualTo(250); // 5 * 50
        }
    }

    @Test
    void testInvalidCsvLineReturnsError() {
        List<String> csvLines = List.of(
            "alice,Alice,g1,Game1,10,75",
            "invalid,line,with,only,four",
            "bob,Bob,g2,Game2,5,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    @Test
    void testDuplicatePlayerGamePairReturnsError() {
        List<String> csvLines = List.of(
            "alice,Alice,g1,Game1,10,75",
            "alice,Alice,g1,Game1,5,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Err.class);
        if (result instanceof Result.Err<RankedResult, PipelineError> err) {
            assertThat(err.error().message()).contains("Duplicate");
        }
    }

    @Test
    void testThreePlayersCombinedCorrectly() {
        List<String> csvLines = List.of(
            "alice,Alice,g1,Game1,10,75",
            "bob,Bob,g2,Game2,8,60",
            "charlie,Charlie,g3,Game3,5,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<RankedResult, PipelineError> ok) {
            assertThat(ok.value().definiteWinners()).hasSize(3);
            // alice: 750, bob: 480, charlie: 250
            assertThat(ok.value().definiteWinners().get(0).totalScore()).isEqualTo(750);
            assertThat(ok.value().definiteWinners().get(1).totalScore()).isEqualTo(480);
            assertThat(ok.value().definiteWinners().get(2).totalScore()).isEqualTo(250);
        }
    }

    // ─────────────────────────────────────────────────────
    // PROPERTY-BASED TESTS
    // ─────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────
    // PROPERTY TESTS (jqwik @ 100 tries each)
    // ─────────────────────────────────────────────────────

    /**
     * Feature: top-three-high-scores, Property 12: Pipeline composition correctness
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void property12_pipelineComposition(
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 5) String playerName,
            @ForAll @IntRange(min = 1, max = 10) int hours,
            @ForAll @IntRange(min = 1, max = 100) int score
    ) {
        List<String> csvLines = List.of(
            String.format("%s,%s,g1,Game1,%d,%d", playerId, playerName, hours, score)
        );
        
        // Pipeline result
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);
        
        // Manual chaining
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        assertThat(parseResult).isInstanceOf(Result.Ok.class);
        if (parseResult instanceof Result.Ok<List<ScoreRecord>, ParseError> ok1) {
            Result<List<PlayerAggregate>, AggregationError> aggResult = scoreAggregator.aggregate(ok1.value());
            assertThat(aggResult).isInstanceOf(Result.Ok.class);
            if (aggResult instanceof Result.Ok<List<PlayerAggregate>, AggregationError> ok2) {
                RankedResult expectedRanked = leaderboardRanker.rank(ok2.value());
                
                assertThat(pipelineResult).isInstanceOf(Result.Ok.class);
                if (pipelineResult instanceof Result.Ok<RankedResult, PipelineError> ok3) {
                    assertThat(ok3.value().definiteWinners()).hasSize(expectedRanked.definiteWinners().size());
                    assertThat(ok3.value().tiedCandidates()).hasSize(expectedRanked.tiedCandidates().size());
                }
            }
        }
    }

    /**
     * Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
     * Validates: Requirements 4.2
     */
    @Property(tries = 100)
    void property13_pipelinePropagateCsvErrors(
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String playerId
    ) {
        // Create an invalid CSV line with wrong field count
        List<String> csvLines = List.of(
            "valid,line,g1,Game1,10,75",
            String.format("%s,incomplete,fields", playerId),
            "another,valid,g2,Game2,5,50"
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
     * Validates: Requirements 4.3
     */
    @Property(tries = 100)
    void property14_pipelineRejectsDuplicatePairs(
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 5) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 3) String gameId
    ) {
        List<String> csvLines = List.of(
            String.format("%s,%s,%s,Game1,10,75", playerId, playerName, gameId),
            String.format("%s,%s,%s,Game2,5,50", playerId, playerName, gameId)
        );
        
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);
        
        assertThat(result).isInstanceOf(Result.Err.class);
        if (result instanceof Result.Err<RankedResult, PipelineError> err) {
            assertThat(err.error().message()).contains("Duplicate");
        }
    }
}
