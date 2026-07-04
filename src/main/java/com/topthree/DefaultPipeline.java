package com.topthree;

import com.topthree.model.*;

import java.util.*;

public class DefaultPipeline implements TopThreePipeline {

    private final CsvParser csvParser = new DefaultCsvParser();
    private final ScoreAggregator scoreAggregator = new DefaultScoreAggregator();
    private final LeaderboardRanker leaderboardRanker = new DefaultLeaderboardRanker();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Parse
        var parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            var pe = err.error();
            return Result.err(new PipelineError(pe.message(), pe.offendingLine()));
        }
        var records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        // Aggregate
        var aggResult = scoreAggregator.aggregate(records);
        if (aggResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            var ae = err.error();
            return Result.err(new PipelineError(ae.message(), ae.playerId()));
        }
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult).value();

        // Rank
        var ranked = leaderboardRanker.rank(aggregates);
        return Result.ok(ranked);
    }
}
