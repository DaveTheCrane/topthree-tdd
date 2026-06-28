package com.topthree.component;

import com.topthree.model.*;

import java.util.*;

public class TopThreePipelineImpl implements TopThreePipeline {
    private final CsvParser parser;
    private final ScoreAggregator aggregator;
    private final LeaderboardRanker ranker;

    public TopThreePipelineImpl(CsvParser parser, ScoreAggregator aggregator, LeaderboardRanker ranker) {
        this.parser = parser;
        this.aggregator = aggregator;
        this.ranker = ranker;
    }

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Step 1: Parse CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = parser.parseLines(csvLines);
        if (parseResult instanceof Result.Err<List<ScoreRecord>, ParseError> parseErr) {
            return Result.err(new PipelineError(
                "CSV parse error: " + parseErr.error().message(),
                parseErr.error().offendingLine()
            ));
        }

        Result.Ok<List<ScoreRecord>, ParseError> parseOk = (Result.Ok<List<ScoreRecord>, ParseError>) parseResult;
        List<ScoreRecord> records = parseOk.value();

        // Step 2: Check for duplicate (playerId, gameId) pairs
        Set<String> seenPairs = new HashSet<>();
        for (ScoreRecord record : records) {
            String pair = record.player().playerId() + "|" + record.gameEntry().gameId();
            if (seenPairs.contains(pair)) {
                return Result.err(new PipelineError(
                    "Duplicate player/game pair found",
                    pair
                ));
            }
            seenPairs.add(pair);
        }

        // Step 3: Aggregate scores
        Result<List<PlayerAggregate>, AggregationError> aggResult = aggregator.aggregate(records);
        if (aggResult instanceof Result.Err<List<PlayerAggregate>, AggregationError> aggErr) {
            return Result.err(new PipelineError(
                "Aggregation error: " + aggErr.error().message(),
                aggErr.error().playerId()
            ));
        }

        Result.Ok<List<PlayerAggregate>, AggregationError> aggOk = (Result.Ok<List<PlayerAggregate>, AggregationError>) aggResult;
        List<PlayerAggregate> aggregates = aggOk.value();

        // Step 4: Rank players
        RankedResult rankedResult = ranker.rank(aggregates);

        return Result.ok(rankedResult);
    }
}
