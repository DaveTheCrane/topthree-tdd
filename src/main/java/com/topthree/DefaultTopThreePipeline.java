package com.topthree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultTopThreePipeline implements TopThreePipeline {

    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Step 1: Parse all CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            ParseError pe = err.error();
            return new Result.Err<>(new PipelineError(pe.message(), pe.offendingLine()));
        }
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Step 2: Check for duplicate (playerId, gameId) pairs
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.player().playerId() + "|" + record.gameEntry().gameId();
            if (!seen.add(key)) {
                return new Result.Err<>(new PipelineError(
                        "Duplicate player-id/game-id pair",
                        record.player().playerId() + "," + record.gameEntry().gameId()));
            }
        }

        // Step 3: Aggregate
        Result<List<PlayerAggregate>, AggregationError> aggResult = scoreAggregator.aggregate(records);
        if (aggResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            AggregationError ae = err.error();
            return new Result.Err<>(new PipelineError(ae.message(), ae.playerId()));
        }
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        // Step 4: Rank
        RankedResult ranked = leaderboardRanker.rank(aggregates);
        return new Result.Ok<>(ranked);
    }
}
