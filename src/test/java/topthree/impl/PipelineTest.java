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
    
    @Test
    void run_validCsvList_producesCorrectRankedResult() {
        String line1 = "p1,Alice,g1,Chess,2,50";  // 100 points
        String line2 = "p2,Bob,g1,Chess,3,40";    // 120 points
        String line3 = "p3,Charlie,g2,Checkers,1,80"; // 80 points
        
        Result<RankedResult, PipelineError> result = pipeline.run(List.of(line1, line2, line3));
        
        assertTrue(result instanceof Result.Ok);
        RankedResult rankedResult = ((Result.Ok<RankedResult, PipelineError>) result).value();
        
        // Should have 3 definite winners (all players) in descending order
        assertEquals(3, rankedResult.definiteWinners().size());
        assertTrue(rankedResult.tiedCandidates().isEmpty());
        
        // Check order: p2 (120), p1 (100), p3 (80)
        assertEquals("p2", rankedResult.definiteWinners().get(0).playerId());
        assertEquals(120, rankedResult.definiteWinners().get(0).totalScore());
        
        assertEquals("p1", rankedResult.definiteWinners().get(1).playerId());
        assertEquals(100, rankedResult.definiteWinners().get(1).totalScore());
        
        assertEquals("p3", rankedResult.definiteWinners().get(2).playerId());
        assertEquals(80, rankedResult.definiteWinners().get(2).totalScore());
    }
    
    @Test
    void run_invalidCsvLine_returnsPipelineError() {
        String validLine = "p1,Alice,g1,Chess,2,50";
        String invalidLine = "p2,Bob,g1,Chess,invalid,40"; // invalid hours
        
        Result<RankedResult, PipelineError> result = pipeline.run(List.of(validLine, invalidLine));
        
        assertTrue(result instanceof Result.Err);
        PipelineError error = ((Result.Err<RankedResult, PipelineError>) result).error();
        assertTrue(error.message().contains("CSV parse error"));
    }
}