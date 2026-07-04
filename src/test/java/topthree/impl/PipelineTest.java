package topthree.impl;

import topthree.interfaces.CsvParser;
import topthree.interfaces.LeaderboardRanker;
import topthree.interfaces.ScoreAggregator;
import topthree.interfaces.TopThreePipeline;
import topthree.models.PipelineError;
import topthree.models.RankedResult;
import topthree.models.Result;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {
    
    private final CsvParser csvParser = new CsvParserImpl();
    private final ScoreAggregator scoreAggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker leaderboardRanker = new LeaderboardRankerImpl();
    private final TopThreePipeline pipeline = new TopThreePipelineImpl(csvParser, scoreAggregator, leaderboardRanker);
    
    @Test
    void run_emptyInput_returnsEmptyRankedResult() {
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());
        
        assertTrue(result instanceof Result.Ok);
        RankedResult rankedResult = ((Result.Ok<RankedResult, PipelineError>) result).value();
        
        assertTrue(rankedResult.definiteWinners().isEmpty());
        assertTrue(rankedResult.tiedCandidates().isEmpty());
    }
}