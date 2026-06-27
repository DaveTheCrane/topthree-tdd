package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

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

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    // Validates: Requirements 4.1
    @Property(tries = 1000)
    void pipelineCompositionCorrectness(@ForAll("validDistinctCsvLines") List<String> csvLines) {
        // Run via pipeline
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);

        // Run manually: parse → aggregate → rank
        CsvParser csvParser = new CsvParserImpl();
        ScoreAggregator scoreAggregator = new ScoreAggregatorImpl();
        LeaderboardRanker leaderboardRanker = new LeaderboardRankerImpl();

        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        assertInstanceOf(Result.Ok.class, parseResult);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        Result<List<PlayerAggregate>, AggregationError> aggResult = scoreAggregator.aggregate(records);
        assertInstanceOf(Result.Ok.class, aggResult);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        RankedResult manualResult = leaderboardRanker.rank(aggregates);

        // Assert pipeline produces same result as manual chaining
        assertInstanceOf(Result.Ok.class, pipelineResult);
        RankedResult pipelineRanked = ((Result.Ok<RankedResult, PipelineError>) pipelineResult).value();

        assertEquals(manualResult.definiteWinners(), pipelineRanked.definiteWinners());
        assertEquals(manualResult.tiedCandidates(), pipelineRanked.tiedCandidates());
    }

    @Provide
    Arbitrary<List<String>> validDistinctCsvLines() {
        // Generate 1-5 lines with distinct (playerId, gameId) pairs
        return Arbitraries.integers().between(1, 5).flatMap(size -> {
            // Generate 'size' distinct lines
            return Arbitraries.just(size).flatMap(n -> {
                List<Arbitrary<String>> lineArbitraries = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    // Use index-based ids to guarantee uniqueness
                    final int idx = i;
                    Arbitrary<String> line = Combinators.combine(
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),  // playerName
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),  // gameName
                            Arbitraries.integers().between(1, 1000),                       // hoursPlayed
                            Arbitraries.integers().between(1, 100)                         // normalisedScore
                    ).as((playerName, gameName, hours, score) ->
                            "p" + idx + "," + playerName + ",g" + idx + "," + gameName + "," + hours + "," + score
                    );
                    lineArbitraries.add(line);
                }
                return Combinators.combine(lineArbitraries).as(lines -> lines);
            });
        });
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    // Validates: Requirements 4.2
    @Property(tries = 1000)
    void pipelinePropagatesCsvParseErrors(@ForAll("csvLinesWithAtLeastOneInvalid") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithAtLeastOneInvalid() {
        Arbitrary<String> validLine = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.integers().between(1, 1000),
                Arbitraries.integers().between(1, 100)
        ).as((pid, pname, gid, gname, hours, score) ->
                pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score
        );

        // Invalid lines: wrong field count, non-integer hours, out-of-range score
        Arbitrary<String> invalidLine = Arbitraries.oneOf(
                // Too few fields
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                        .map(s -> s + ",field2,field3"),
                // Non-integer hours-played
                Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                ).as((pid, pname, gid, gname) ->
                        pid + "," + pname + "," + gid + "," + gname + ",notAnInt,50"
                ),
                // Out-of-range normalised score
                Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.integers().between(1, 100),
                        Arbitraries.oneOf(
                                Arbitraries.integers().between(-1000, 0),
                                Arbitraries.integers().between(101, 1000)
                        )
                ).as((pid, pname, gid, gname, hours, score) ->
                        pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score
                )
        );

        // Generate 0-4 valid lines + at least 1 invalid, then shuffle
        return Combinators.combine(
                validLine.list().ofMinSize(0).ofMaxSize(4),
                invalidLine.list().ofMinSize(1).ofMaxSize(3)
        ).as((valids, invalids) -> {
            List<String> all = new ArrayList<>(valids);
            all.addAll(invalids);
            Collections.shuffle(all);
            return all;
        });
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    // Validates: Requirements 4.3
    @Property(tries = 1000)
    void pipelineRejectsDuplicatePlayerIdGameIdPairs(@ForAll("csvLinesWithDuplicate") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        Result.Err<RankedResult, PipelineError> err = (Result.Err<RankedResult, PipelineError>) result;
        assertTrue(err.error().message().toLowerCase().contains("duplicate"));
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithDuplicate() {
        // Generate 1-4 valid lines with distinct pairs, then duplicate one entry
        return Arbitraries.integers().between(1, 4).flatMap(size -> {
            return Arbitraries.just(size).flatMap(n -> {
                List<Arbitrary<String>> lineArbitraries = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    final int idx = i;
                    Arbitrary<String> line = Combinators.combine(
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                            Arbitraries.integers().between(1, 1000),
                            Arbitraries.integers().between(1, 100)
                    ).as((playerName, gameName, hours, score) ->
                            "p" + idx + "," + playerName + ",g" + idx + "," + gameName + "," + hours + "," + score
                    );
                    lineArbitraries.add(line);
                }
                return Combinators.combine(lineArbitraries).as(lines -> {
                    List<String> result = new ArrayList<>(lines);
                    // Pick a random existing line to duplicate (same playerId, gameId but possibly different other fields)
                    String original = result.get(0);
                    String[] fields = original.split(",");
                    // Create a duplicate with same playerId and gameId but different hours/score
                    String duplicate = fields[0] + "," + fields[1] + "," + fields[2] + "," + fields[3] + ",1,50";
                    result.add(duplicate);
                    return result;
                });
            });
        });
    }
}
