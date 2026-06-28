package com.topthree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TopThreePipeline that chains CsvParser, ScoreAggregator, and LeaderboardRanker.
 */
public class TopThreePipelineImpl implements TopThreePipeline {
    
    private final CsvParser csvParser;
    private final ScoreAggregator scoreAggregator;
    private final LeaderboardRanker leaderboardRanker;

    public TopThreePipelineImpl(CsvParser csvParser, ScoreAggregator scoreAggregator, LeaderboardRanker leaderboardRanker) {
        this.csvParser = csvParser;
        this.scoreAggregator = scoreAggregator;
        this.leaderboardRanker = leaderboardRanker;
    }

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Handle empty input
        if (csvLines == null || csvLines.isEmpty()) {
            return new Result.Ok<>(new RankedResult(new ArrayList<>(), new ArrayList<>()));
        }

        // Step 1: Parse CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            return new Result.Err<>(new PipelineError(
                "CSV parse error: " + err.error().message(),
                err.error().offendingLine()
            ));
        }

        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Step 2: Check for duplicates (same playerId, gameId combination)
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.player().playerId() + ":" + record.gameEntry().gameId();
            if (!seen.add(key)) {
                return new Result.Err<>(new PipelineError(
                    "Duplicate player-game pair detected",
                    record.player().playerId() + "/" + record.gameEntry().gameId()
                ));
            }
        }

        // Step 3: Aggregate scores
        Result<List<PlayerAggregate>, AggregationError> aggregateResult = scoreAggregator.aggregate(records);
        if (aggregateResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            return new Result.Err<>(new PipelineError(
                "Aggregation error: " + err.error().message(),
                err.error().playerId()
            ));
        }

        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregateResult).value();

        // Step 4: Rank players
        RankedResult rankedResult = leaderboardRanker.rank(aggregates);

        return new Result.Ok<>(rankedResult);
    }
}
