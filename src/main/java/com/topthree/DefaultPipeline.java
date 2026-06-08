package com.topthree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultPipeline implements TopThreePipeline {

    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Step 1: Parse CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            ParseError parseError = err.error();
            return new Result.Err<>(new PipelineError(parseError.message(), parseError.offendingLine()));
        }
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Step 2: Check for duplicate (playerId, gameId) pairs
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.player().playerId() + ":" + record.gameEntry().gameId();
            if (!seen.add(key)) {
                return new Result.Err<>(new PipelineError("Duplicate player/game pair",
                        record.player().playerId() + "/" + record.gameEntry().gameId()));
            }
        }

        // Step 3: Aggregate scores
        Result<List<PlayerAggregate>, AggregationError> aggregateResult = scoreAggregator.aggregate(records);
        if (aggregateResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            AggregationError aggError = err.error();
            return new Result.Err<>(new PipelineError(aggError.message(), aggError.playerId()));
        }
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregateResult).value();

        // Step 4: Rank players
        RankedResult rankedResult = leaderboardRanker.rank(aggregates);
        return new Result.Ok<>(rankedResult);
    }
}
