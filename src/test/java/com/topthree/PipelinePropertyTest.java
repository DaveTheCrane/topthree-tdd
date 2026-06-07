package com.topthree;

import net.jqwik.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PipelinePropertyTest {

    private final TopThreePipeline pipeline = new DefaultTopThreePipeline();
    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    // **Validates: Requirements 4.1**
    @Property(tries = 1000)
    void pipelineCompositionCorrectness(
            @ForAll("validCsvLineLists") List<String> csvLines
    ) {
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);

        // Manually chain the three components
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        assertInstanceOf(Result.Ok.class, parseResult);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        Result<List<PlayerAggregate>, AggregationError> aggResult = scoreAggregator.aggregate(records);
        assertInstanceOf(Result.Ok.class, aggResult);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        RankedResult expectedRanked = leaderboardRanker.rank(aggregates);

        // Pipeline should produce the same result
        assertInstanceOf(Result.Ok.class, pipelineResult);
        RankedResult actualRanked = ((Result.Ok<RankedResult, PipelineError>) pipelineResult).value();

        assertEquals(expectedRanked.definiteWinners(), actualRanked.definiteWinners());
        assertEquals(expectedRanked.tiedCandidates(), actualRanked.tiedCandidates());
    }

    @Provide
    Arbitrary<List<String>> validCsvLineLists() {
        // Generate 1-5 unique (playerId, gameId) combinations as valid CSV lines
        return Arbitraries.integers().between(1, 5).flatMap(size ->
                validCsvLineWithUniqueKeys().list().ofSize(size).filter(lines -> {
                    // Ensure no duplicate (playerId, gameId) pairs
                    Set<String> seen = new HashSet<>();
                    for (String line : lines) {
                        String[] fields = line.split(",");
                        String key = fields[0] + "|" + fields[2];
                        if (!seen.add(key)) {
                            return false;
                        }
                    }
                    return true;
                })
        );
    }

    private Arbitrary<String> validCsvLineWithUniqueKeys() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(8),
                Arbitraries.integers().between(1, 100),
                Arbitraries.integers().between(1, 100)
        ).as((pid, pname, gid, gname, hours, score) ->
                pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + score
        );
    }
}
