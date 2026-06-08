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
