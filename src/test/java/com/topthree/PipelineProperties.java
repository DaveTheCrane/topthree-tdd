package com.topthree;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineProperties {

    private final TopThreePipeline pipeline = new DefaultPipeline();
    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    // **Validates: Requirements 4.1**
    @Property(tries = 1000)
    void pipelineCompositionCorrectness(@ForAll("validUniqueCsvLines") List<String> csvLines) {
        // Run the pipeline
        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(csvLines);

        // Manually chain: parse → aggregate → rank
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        assertThat(parseResult).isInstanceOf(Result.Ok.class);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        Result<List<PlayerAggregate>, AggregationError> aggregateResult = scoreAggregator.aggregate(records);
        assertThat(aggregateResult).isInstanceOf(Result.Ok.class);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregateResult).value();

        RankedResult manualResult = leaderboardRanker.rank(aggregates);

        // Assert the pipeline result matches the manual chain result
        assertThat(pipelineResult).isInstanceOf(Result.Ok.class);
        RankedResult pipelineRanked = ((Result.Ok<RankedResult, PipelineError>) pipelineResult).value();

        assertThat(pipelineRanked.definiteWinners()).isEqualTo(manualResult.definiteWinners());
        assertThat(pipelineRanked.tiedCandidates()).isEqualTo(manualResult.tiedCandidates());
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    // **Validates: Requirements 4.2**
    @Property(tries = 1000)
    void pipelinePropagatesCsvParseErrors(@ForAll("csvLinesWithAtLeastOneInvalid") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertThat(result).isInstanceOf(Result.Err.class);
        PipelineError error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertThat(error).isNotNull();
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithAtLeastOneInvalid() {
        // Generate 1-5 valid lines, then inject one invalid line at a random position
        Arbitrary<Integer> validCountArb = Arbitraries.integers().between(0, 4);
        Arbitrary<String> invalidLineArb = Arbitraries.oneOf(
                // Just a word (no commas at all)
                Arbitraries.strings().ofMinLength(1).ofMaxLength(10).alpha().map(s -> s),
                // Too few fields (e.g., 3 fields)
                Arbitraries.of("a,b,c", "p1,Alice,g1"),
                // Too many fields (7 fields)
                Arbitraries.of("p1,Alice,g1,Chess,2,50,extra", "a,b,c,d,e,f,g"),
                // Non-integer hours-played
                Arbitraries.of("p1,Alice,g1,Chess,abc,50", "p1,Alice,g1,Chess,1.5,50"),
                // Non-integer normalised-score
                Arbitraries.of("p1,Alice,g1,Chess,2,xyz", "p1,Alice,g1,Chess,2,3.14"),
                // Out-of-range normalised score (0 or 101+)
                Arbitraries.of("p1,Alice,g1,Chess,2,0", "p1,Alice,g1,Chess,2,101", "p1,Alice,g1,Chess,2,-5")
        );

        return Combinators.combine(validCountArb, invalidLineArb).flatAs((validCount, invalidLine) -> {
            Arbitrary<String> alphaArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(8).alpha();
            Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 500);
            Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

            return Combinators.combine(
                    alphaArb.list().ofSize(validCount),
                    alphaArb.list().ofSize(validCount),
                    alphaArb.list().ofSize(validCount),
                    alphaArb.list().ofSize(validCount),
                    hoursArb.list().ofSize(validCount),
                    scoreArb.list().ofSize(validCount)
            ).as((playerIds, playerNames, gameIds, gameNames, hours, scores) -> {
                List<String> validLines = new ArrayList<>();
                for (int i = 0; i < validCount; i++) {
                    String playerId = "p" + i + playerIds.get(i);
                    String playerName = playerNames.get(i);
                    String gameId = "g" + i + gameIds.get(i);
                    String gameName = gameNames.get(i);
                    String line = String.join(",", playerId, playerName, gameId, gameName,
                            String.valueOf(hours.get(i)), String.valueOf(scores.get(i)));
                    validLines.add(line);
                }

                // Inject invalid line at a random position
                List<String> allLines = new ArrayList<>(validLines);
                int insertPos = validLines.size() > 0 ? new Random(invalidLine.hashCode()).nextInt(validLines.size() + 1) : 0;
                allLines.add(insertPos, invalidLine);
                return allLines;
            });
        });
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    // **Validates: Requirements 4.3**
    @Property(tries = 1000)
    void pipelineRejectsDuplicatePlayerGamePairs(@ForAll("csvLinesWithDuplicatePlayerGamePair") List<String> csvLines) {
        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertThat(result).isInstanceOf(Result.Err.class);
        PipelineError error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertThat(error).isNotNull();
    }

    @Provide
    Arbitrary<List<String>> csvLinesWithDuplicatePlayerGamePair() {
        return Arbitraries.integers().between(1, 5).flatMap(lineCount -> {
            Arbitrary<String> alphaArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(8).alpha();
            Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 500);
            Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

            return Combinators.combine(
                    alphaArb.list().ofSize(lineCount),  // playerIds
                    alphaArb.list().ofSize(lineCount),  // playerNames
                    alphaArb.list().ofSize(lineCount),  // gameIds
                    alphaArb.list().ofSize(lineCount),  // gameNames
                    hoursArb.list().ofSize(lineCount),  // hoursPlayed
                    scoreArb.list().ofSize(lineCount)   // normalisedScores
            ).as((playerIds, playerNames, gameIds, gameNames, hours, scores) -> {
                // Build unique lines (same logic as validUniqueCsvLines)
                List<String> lines = new ArrayList<>();
                for (int i = 0; i < lineCount; i++) {
                    String playerId = "p" + i + playerIds.get(i);
                    String playerName = playerNames.get(i);
                    String gameId = "g" + i + gameIds.get(i);
                    String gameName = gameNames.get(i);
                    String line = String.join(",", playerId, playerName, gameId, gameName,
                            String.valueOf(hours.get(i)), String.valueOf(scores.get(i)));
                    lines.add(line);
                }
                return lines;
            }).flatMap(lines -> {
                // Pick one existing line to duplicate its (playerId, gameId)
                int size = lines.size();
                return Combinators.combine(
                        Arbitraries.integers().between(0, size - 1),
                        alphaArb,
                        hoursArb,
                        scoreArb
                ).as((dupIndex, dupName, dupHours, dupScore) -> {
                    List<String> result = new ArrayList<>(lines);
                    // Extract playerId and gameId from the chosen line
                    String[] fields = lines.get(dupIndex).split(",");
                    String dupPlayerId = fields[0];
                    String dupGameId = fields[2];
                    String dupGameName = fields[3];
                    String duplicateLine = String.join(",", dupPlayerId, dupName, dupGameId, dupGameName,
                            String.valueOf(dupHours), String.valueOf(dupScore));
                    result.add(duplicateLine);
                    return result;
                });
            });
        });
    }

    @Provide
    Arbitrary<List<String>> validUniqueCsvLines() {
        return Arbitraries.integers().between(1, 5).flatMap(lineCount -> {
            // Generate lineCount unique (playerId, gameId) pairs with valid data
            Arbitrary<String> alphaArb = Arbitraries.strings().ofMinLength(1).ofMaxLength(8).alpha();
            Arbitrary<Integer> hoursArb = Arbitraries.integers().between(1, 500);
            Arbitrary<Integer> scoreArb = Arbitraries.integers().between(1, 100);

            // Generate lists of components
            return Combinators.combine(
                    alphaArb.list().ofSize(lineCount),  // playerIds
                    alphaArb.list().ofSize(lineCount),  // playerNames
                    alphaArb.list().ofSize(lineCount),  // gameIds
                    alphaArb.list().ofSize(lineCount),  // gameNames
                    hoursArb.list().ofSize(lineCount),  // hoursPlayed
                    scoreArb.list().ofSize(lineCount)   // normalisedScores
            ).as((playerIds, playerNames, gameIds, gameNames, hours, scores) -> {
                // Ensure unique (playerId, gameId) pairs by appending index suffixes
                List<String> lines = new ArrayList<>();
                for (int i = 0; i < lineCount; i++) {
                    String playerId = "p" + i + playerIds.get(i);
                    String playerName = playerNames.get(i);
                    String gameId = "g" + i + gameIds.get(i);
                    String gameName = gameNames.get(i);
                    String line = String.join(",", playerId, playerName, gameId, gameName,
                            String.valueOf(hours.get(i)), String.valueOf(scores.get(i)));
                    lines.add(line);
                }
                return lines;
            });
        });
    }
}
