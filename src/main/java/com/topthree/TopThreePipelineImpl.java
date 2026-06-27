package com.topthree;

import java.util.List;

public class TopThreePipelineImpl implements TopThreePipeline {

    private final CsvParser parser = new CsvParserImpl();
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        var parseResult = parser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            var pe = err.error();
            return new Result.Err<>(new PipelineError(pe.message(), pe.offendingLine()));
        }
        var records = ((Result.Ok<List<ScoreRecord>, ParseError>) parseResult).value();

        var aggregateResult = aggregator.aggregate(records);
        if (aggregateResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            var ae = err.error();
            return new Result.Err<>(new PipelineError(ae.message(), ae.playerId()));
        }
        var aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregateResult).value();

        var rankedResult = ranker.rank(aggregates);
        return new Result.Ok<>(rankedResult);
    }
}
