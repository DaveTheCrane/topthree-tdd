package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.interfaces.LeaderboardRanker;
import topthree.interfaces.ScoreAggregator;
import topthree.interfaces.TopThreePipeline;
import topthree.models.AggregationError;
import topthree.models.PipelineError;
import topthree.models.ParseError;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;
import topthree.models.Result;
import topthree.models.ScoreRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static topthree.models.Result.Err;
import static topthree.models.Result.Ok;

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
        // Step 1: Parse CSV lines
        Result<List<ScoreRecord>, ParseError> parseResult = csvParser.parseLines(csvLines);
        if (!parseResult.isOk()) {
            return new Err<>(new PipelineError("CSV parse error: " + parseResult.error().message()));
        }

        List<ScoreRecord> records = parseResult.get();

        // Step 2: Check for duplicate playerId/gameId pairs
        Set<String> seen = new HashSet<>();
        for (ScoreRecord record : records) {
            String key = record.playerId() + ":" + record.gameId();
            if (seen.contains(key)) {
                return new Err<>(new PipelineError("Duplicate playerId/gameId pair: " + key));
            }
            seen.add(key);
        }

        // Step 3: Aggregate scores
        Result<List<PlayerAggregate>, AggregationError> aggregateResult = scoreAggregator.aggregate(records);
        if (!aggregateResult.isOk()) {
            return new Err<>(new PipelineError("Aggregation error: " + aggregateResult.error().message()));
        }

        // Step 4: Rank players
        List<PlayerAggregate> aggregates = aggregateResult.get();
        RankedResult rankedResult = leaderboardRanker.rank(aggregates);

        return new Ok<>(rankedResult);
    }
}
