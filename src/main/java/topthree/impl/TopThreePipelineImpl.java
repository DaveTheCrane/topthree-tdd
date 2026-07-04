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
        // Will be implemented through TDD
        return new Result.Err<>(new PipelineError("Not implemented yet"));
    }
}