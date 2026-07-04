package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.interfaces.LeaderboardRanker;
import topthree.interfaces.ScoreAggregator;
import topthree.interfaces.TopThreePipeline;
import topthree.models.PipelineError;
import topthree.models.RankedResult;
import topthree.models.Result;
import java.util.List;

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
        if (csvLines.isEmpty()) {
            return new Result.Ok<>(new RankedResult(List.of(), List.of()));
        }
        
        // Parse CSV lines
        var parseResult = csvParser.parseLines(csvLines);
        if (parseResult instanceof topthree.models.Result.Err) {
            var parseError = ((topthree.models.Result.Err<java.util.List<topthree.models.ScoreRecord>, topthree.models.ParseError>) parseResult).error();
            return new Result.Err<>(new PipelineError("CSV parse error: " + parseError.message()));
        }
        
        var records = ((topthree.models.Result.Ok<java.util.List<topthree.models.ScoreRecord>, topthree.models.ParseError>) parseResult).value();
        
        // Aggregate scores
        var aggregateResult = scoreAggregator.aggregate(records);
        if (aggregateResult instanceof topthree.models.Result.Err) {
            var aggregationError = ((topthree.models.Result.Err<java.util.List<topthree.models.PlayerAggregate>, topthree.models.AggregationError>) aggregateResult).error();
            return new Result.Err<>(new PipelineError("Aggregation error: " + aggregationError.message()));
        }
        
        var aggregates = ((topthree.models.Result.Ok<java.util.List<topthree.models.PlayerAggregate>, topthree.models.AggregationError>) aggregateResult).value();
        
        // Rank players
        var rankedResult = leaderboardRanker.rank(aggregates);
        
        return new Result.Ok<>(rankedResult);
    }
}