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

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    // **Validates: Requirements 4.2**
    @Property(tries = 1000)
    void pipelinePropagatesCsvParseErrors(
            @ForAll("csvLineListsWithAtLeastOneInvalid") List<String> csvLines
    ) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<List<String>> csvLineListsWithAtLeastOneInvalid() {
        Arbitrary<String> validLine = validCsvLineWithUniqueKeys();
        Arbitrary<String> invalidLine = invalidCsvLines();

        return Combinators.combine(
                validLine.list().ofMinSize(0).ofMaxSize(3),
                invalidLine,
                validLine.list().ofMinSize(0).ofMaxSize(3)
        ).as((before, invalid, after) -> {
            List<String> lines = new ArrayList<>(before);
            lines.add(invalid);
            lines.addAll(after);
            return lines;
        });
    }

    @Provide
    Arbitrary<String> invalidCsvLines() {
        return Arbitraries.oneOf(
                // Wrong field count (too few)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                        .list().ofMinSize(1).ofMaxSize(5)
                        .map(fields -> String.join(",", fields)),
                // Wrong field count (too many)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5)
                        .list().ofSize(7)
                        .map(fields -> String.join(",", fields)),
                // Non-integer hours played
                Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(3),
                        Arbitraries.integers().between(1, 100)
                ).as((pid, pname, gid, gname, badHours, score) ->
                        pid + "," + pname + "," + gid + "," + gname + "," + badHours + "," + score
                ),
                // Out-of-range normalised score
                Combinators.combine(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                        Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(5),
                        Arbitraries.integers().between(1, 100),
                        Arbitraries.oneOf(
                                Arbitraries.integers().lessOrEqual(0),
                                Arbitraries.integers().greaterOrEqual(101)
                        )
                ).as((pid, pname, gid, gname, hours, badScore) ->
                        pid + "," + pname + "," + gid + "," + gname + "," + hours + "," + badScore
                )
        );
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    // **Validates: Requirements 4.3**
    @Property(tries = 1000)
    void pipelineRejectsDuplicatePlayerIdGameIdPairs(
            @ForAll("csvLineListsWithDuplicate") List<String> csvLines
    ) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertInstanceOf(Result.Err.class, result);
    }

    @Provide
    Arbitrary<List<String>> csvLineListsWithDuplicate() {
        // Generate valid lines, then inject a duplicate (playerId, gameId) pair
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),  // shared playerId
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(8),  // playerName
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),  // shared gameId
                Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(8),  // gameName
                Arbitraries.integers().between(1, 100),                        // hours1
                Arbitraries.integers().between(1, 100),                        // score1
                Arbitraries.integers().between(1, 100),                        // hours2
                Arbitraries.integers().between(1, 100)                         // score2
        ).as((pid, pname, gid, gname, h1, s1, h2, s2) -> {
            String line1 = pid + "," + pname + "," + gid + "," + gname + "," + h1 + "," + s1;
            String line2 = pid + "," + pname + "," + gid + "," + gname + "," + h2 + "," + s2;
            List<String> lines = new ArrayList<>();
            lines.add(line1);
            lines.add(line2);
            return lines;
        });
    }
}
