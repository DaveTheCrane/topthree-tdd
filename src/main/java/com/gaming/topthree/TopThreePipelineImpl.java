package com.gaming.topthree;

import java.util.List;

/**
 * Default {@link TopThreePipeline} implementation. Wires the CSV parser, score
 * aggregator, and leaderboard ranker end-to-end.
 */
public class TopThreePipelineImpl implements TopThreePipeline {

    private final CsvParser csvParser;
    private final ScoreAggregator scoreAggregator;
    private final LeaderboardRanker leaderboardRanker;

    public TopThreePipelineImpl(CsvParser csvParser,
                                ScoreAggregator scoreAggregator,
                                LeaderboardRanker leaderboardRanker) {
        this.csvParser = csvParser;
        this.scoreAggregator = scoreAggregator;
        this.leaderboardRanker = leaderboardRanker;
    }

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        Result<List<ScoreRecord>, ParseError> parsed = csvParser.parseLines(csvLines);
        if (parsed instanceof Result.Err<List<ScoreRecord>, ParseError> err) {
            ParseError error = err.error();
            return Result.err(new PipelineError(error.message(), error.offendingLine()));
        }
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parsed).value();

        Result<List<PlayerAggregate>, AggregationError> aggregated = scoreAggregator.aggregate(records);
        if (aggregated instanceof Result.Err<List<PlayerAggregate>, AggregationError> err) {
            AggregationError error = err.error();
            return Result.err(new PipelineError(error.message(), error.playerId()));
        }
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregated).value();

        RankedResult ranked = leaderboardRanker.rank(aggregates);
        return Result.ok(ranked);
    }
}
