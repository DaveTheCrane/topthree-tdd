package com.topthree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopThreePipelineImpl implements TopThreePipeline {

    private final CsvParser csvParser = new CsvParserImpl();
    private final ScoreAggregator scoreAggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker leaderboardRanker = new LeaderboardRankerImpl();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Step 1: Parse CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            return new Result.Err<>(new PipelineError(err.error().message(), err.error().offendingLine()));
        }
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Step 2: Check for duplicate (playerId, gameId) pairs
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.player().playerId() + "|" + record.gameEntry().gameId();
            if (!seen.add(key)) {
                return new Result.Err<>(new PipelineError(
                        "Duplicate (playerId, gameId) pair: (" + record.player().playerId() + ", " + record.gameEntry().gameId() + ")",
                        record.player().playerId() + "," + record.gameEntry().gameId()));
            }
        }

        // Step 3: Aggregate scores
        Result<List<PlayerAggregate>, AggregationError> aggResult = scoreAggregator.aggregate(records);
        if (aggResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            return new Result.Err<>(new PipelineError(err.error().message(), err.error().playerId()));
        }
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        // Step 4: Rank
        RankedResult rankedResult = leaderboardRanker.rank(aggregates);

        return new Result.Ok<>(rankedResult);
    }
}
