package com.topthree;

import com.topthree.model.*;
import net.jqwik.api.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PipelinePropertyTest {

    private final TopThreePipeline pipeline = new DefaultPipeline();
    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();
    private final PrettyPrinter printer = new DefaultPrettyPrinter();

    // --- Generators ---

    @Provide
    Arbitrary<String> playerIds() {
        return Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(5)
                .map(s -> "p" + s);
    }

    @Provide
    Arbitrary<String> gameIds() {
        return Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(5)
                .map(s -> "g" + s);
    }

    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
    }

    @Provide
    Arbitrary<List<String>> validCsvLines() {
        // Generate unique (playerId, gameId) combinations to avoid duplicates
        return Combinators.combine(
                playerIds().list().ofMinSize(1).ofMaxSize(5),
                gameIds().list().ofMinSize(1).ofMaxSize(3),
                names(),
                names(),
                Arbitraries.integers().between(1, 50),
                Arbitraries.integers().between(1, 100)
        ).as((pids, gids, pname, gname, hours, score) -> {
            List<String> lines = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (String pid : pids) {
                for (String gid : gids) {
                    String key = pid + "|" + gid;
                    if (seen.add(key)) {
                        lines.add(pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score);
                    }
                }
            }
            return lines;
        }).filter(list -> !list.isEmpty());
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithInvalid() {
        return validCsvLines().map(lines -> {
            List<String> result = new ArrayList<>(lines);
            result.add("invalid,line,missing,fields");
            return result;
        });
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithDuplicate() {
        return Combinators.combine(
                playerIds(),
                names(),
                gameIds(),
                names(),
                Arbitraries.integers().between(1, 50),
                Arbitraries.integers().between(1, 100)
        ).as((pid, pname, gid, gname, hours, score) -> {
            String line = pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score;
            return List.of(line, line); // exact duplicate (playerId, gameId)
        });
    }

    // --- Property Tests ---

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    @Property(tries = 1000)
    void pipelineCompositionCorrectness(@ForAll("validCsvLines") List<String> csvLines) {
        // Run via pipeline
        var pipelineResult = pipeline.run(csvLines);

        // Run manually: parse -> aggregate -> rank
        var parseResult = csvParser.parseLines(csvLines);
        assertInstanceOf(Result.Ok.class, parseResult);
        var records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        var aggResult = scoreAggregator.aggregate(records);
        assertInstanceOf(Result.Ok.class, aggResult);
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        var expectedRanked = leaderboardRanker.rank(aggregates);

        // Pipeline should succeed and match manual chaining
        assertInstanceOf(Result.Ok.class, pipelineResult);
        var actualRanked = ((Result.Ok<RankedResult, PipelineError>) pipelineResult).value();
        assertEquals(expectedRanked, actualRanked);
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    @Property(tries = 1000)
    void pipelinePropagatesCsvParseErrors(@ForAll("csvLinesWithInvalid") List<String> csvLines) {
        var result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result,
                "Pipeline should return error when input contains an invalid CSV line");
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    @Property(tries = 1000)
    void pipelineRejectsDuplicatePlayerGamePairs(@ForAll("csvLinesWithDuplicate") List<String> csvLines) {
        var result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result,
                "Pipeline should return error when input contains duplicate playerId/gameId pairs");
        var error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertTrue(error.message().toLowerCase().contains("duplicate"),
                "Error message should mention 'duplicate', got: " + error.message());
    }
}
