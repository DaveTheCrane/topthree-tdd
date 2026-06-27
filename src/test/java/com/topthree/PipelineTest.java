package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private final TopThreePipeline pipeline = new TopThreePipelineImpl();
    private final CsvParser parser = new CsvParserImpl();
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

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

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    // Validates: Requirements 4.1
    @Property(tries = 1000)
    void pipelineCompositionCorrectness(
            @ForAll("validCsvLineLists") List<String> csvLines
    ) {
        var pipelineResult = pipeline.run(csvLines);

        // Manual chaining: parse → aggregate → rank
        var parseResult = parser.parseLines(csvLines);
        assertInstanceOf(Result.Ok.class, parseResult);
        var records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Verify no duplicate (playerId, gameId) pairs
        var seen = new HashSet<String>();
        for (var record : records) {
            var key = record.player().playerId() + "|" + record.gameEntry().gameId();
            assertTrue(seen.add(key), "Generated data should have distinct playerId/gameId pairs");
        }

        var aggregateResult = aggregator.aggregate(records);
        assertInstanceOf(Result.Ok.class, aggregateResult);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregateResult).value();

        var rankedResult = ranker.rank(aggregates);
        var expectedResult = new Result.Ok<RankedResult, PipelineError>(rankedResult);

        assertEquals(expectedResult, pipelineResult);
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    // Validates: Requirements 4.2
    @Property(tries = 1000)
    void pipelinePropagatesCsvParseErrors(
            @ForAll("csvLineListsWithAtLeastOneInvalid") List<String> csvLines
    ) {
        var result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
        var err = (Result.Err<RankedResult, PipelineError>) result;
        assertNotNull(err.error());
        assertInstanceOf(PipelineError.class, err.error());
    }

    @Provide
    Arbitrary<List<String>> csvLineListsWithAtLeastOneInvalid() {
        Arbitrary<String> validLineArb = validCsvLine();
        Arbitrary<String> invalidLineArb = invalidCsvLine();

        return Combinators.combine(
                validLineArb.list().ofMinSize(0).ofMaxSize(2),
                invalidLineArb
        ).as((validLines, invalidLine) -> {
            List<String> combined = new ArrayList<>(validLines);
            combined.add(invalidLine);
            Collections.shuffle(combined);
            return combined;
        });
    }

    private Arbitrary<String> validCsvLine() {
        Arbitrary<String> playerIdArb = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> playerNameArb = Arbitraries.strings()
                .alpha().numeric().withChars('-', '_')
                .ofMinLength(1).ofMaxLength(15)
                .filter(s -> !s.contains(","));
        Arbitrary<String> gameIdArb = Arbitraries.strings()
                .alpha().numeric().withChars('-')
                .ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> gameNameArb = Arbitraries.strings()
                .alpha().numeric().withChars('-', '_')
                .ofMinLength(1).ofMaxLength(15)
                .filter(s -> !s.contains(","));
        Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 100);
        Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

        return Combinators.combine(playerIdArb, playerNameArb, gameIdArb, gameNameArb, hoursArb, scoreArb)
                .as((pid, pname, gid, gname, hours, score) ->
                        String.join(",", pid, pname, gid, gname,
                                String.valueOf(hours), String.valueOf(score)));
    }

    private Arbitrary<String> invalidCsvLine() {
        // Multiple types of invalid lines
        Arbitrary<String> wrongFieldCount = Arbitraries.of(
                "p1,Alice,g1",           // too few fields
                "p1,Alice,g1,Chess,10",  // 5 fields
                "p1,Alice,g1,Chess,10,50,extra"  // 7 fields
        );

        Arbitrary<String> nonIntegerHours = Arbitraries.of(
                "p1,Alice,g1,Chess,abc,50",
                "p1,Alice,g1,Chess,1.5,50",
                "p1,Alice,g1,Chess,,50"
        );

        Arbitrary<String> nonIntegerScore = Arbitraries.of(
                "p1,Alice,g1,Chess,10,xyz",
                "p1,Alice,g1,Chess,10,1.5",
                "p1,Alice,g1,Chess,10,"
        );

        Arbitrary<String> outOfRangeScore = Arbitraries.of(
                "p1,Alice,g1,Chess,10,0",
                "p1,Alice,g1,Chess,10,-5",
                "p1,Alice,g1,Chess,10,101",
                "p1,Alice,g1,Chess,10,999"
        );

        Arbitrary<String> emptyIds = Arbitraries.of(
                ",Alice,g1,Chess,10,50",      // empty player id
                "p1,Alice,,Chess,10,50"       // empty game id
        );

        return Arbitraries.oneOf(wrongFieldCount, nonIntegerHours, nonIntegerScore, outOfRangeScore, emptyIds);
    }

    @Provide
    Arbitrary<List<String>> validCsvLineLists() {
        return Arbitraries.integers().between(1, 5).flatMap(size ->
            Arbitraries.just(size).flatMap(n -> {
                // Generate n distinct (playerId, gameId) pairs with valid fields
                Arbitrary<String> playerIdArb = Arbitraries.strings()
                        .alpha().numeric()
                        .ofMinLength(1).ofMaxLength(10);
                Arbitrary<String> playerNameArb = Arbitraries.strings()
                        .alpha().numeric().withChars('-', '_')
                        .ofMinLength(1).ofMaxLength(15)
                        .filter(s -> !s.contains(","));
                Arbitrary<String> gameIdArb = Arbitraries.strings()
                        .alpha().numeric().withChars('-')
                        .ofMinLength(1).ofMaxLength(10);
                Arbitrary<String> gameNameArb = Arbitraries.strings()
                        .alpha().numeric().withChars('-', '_')
                        .ofMinLength(1).ofMaxLength(15)
                        .filter(s -> !s.contains(","));
                Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 100);
                Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

                return Combinators.combine(playerIdArb, playerNameArb, gameIdArb, gameNameArb, hoursArb, scoreArb)
                        .as((pid, pname, gid, gname, hours, score) ->
                                String.join(",", pid, pname, gid, gname,
                                        String.valueOf(hours), String.valueOf(score)))
                        .list().ofSize(n)
                        .map(lines -> {
                            // Ensure distinct (playerId, gameId) pairs by appending index
                            List<String> result = new ArrayList<>();
                            for (int i = 0; i < lines.size(); i++) {
                                String[] fields = lines.get(i).split(",", -1);
                                // Make playerId and gameId unique per line by appending index
                                fields[0] = fields[0] + i;
                                fields[2] = fields[2] + i;
                                result.add(String.join(",", fields));
                            }
                            return result;
                        });
            })
        );
    }
}
