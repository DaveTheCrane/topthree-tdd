package com.topthree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of TopThreePipeline interface.
 */
public class TopThreePipelineImpl implements TopThreePipeline {

    private final CsvParser csvParser = new CsvParserImpl();
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();

    @Override
    public Result<RankedResult, PipelineError> run(List<String> csvLines) {
        // Parse the CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (parseResult.isErr()) {
            return Result.err(new PipelineError("CSV parse error: " + parseResult.getError().message(), "parse"));
        }
        
        List<ScoreRecord> records = parseResult.get();
        
        // Check for duplicate (playerId, gameId) pairs
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.player().playerId() + "|" + record.gameEntry().gameId();
            if (seen.contains(key)) {
                return Result.err(new PipelineError("Duplicate player/game pair: " + key, "duplicate"));
            }
            seen.add(key);
        }
        
        // Aggregate the scores
        Result<List<PlayerAggregate>, AggregationError> aggregateResult = aggregator.aggregate(records);
        if (aggregateResult.isErr()) {
            return Result.err(new PipelineError("Aggregation error: " + aggregateResult.getError().message(), "aggregate"));
        }
        
        // Rank the players
        List<PlayerAggregate> aggregates = aggregateResult.get();
        RankedResult rankedResult = ranker.rank(aggregates);
        
        return Result.ok(rankedResult);
    }
}
