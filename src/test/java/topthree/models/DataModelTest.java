package topthree.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DataModelTest {
    
    @Test
    void testResultOk() {
        Result<String, Integer> ok = new Result.Ok<>("success");
        assertTrue(ok instanceof Result.Ok);
        assertEquals("success", ((Result.Ok<String, Integer>) ok).value());
    }
    
    @Test
    void testResultErr() {
        Result<String, Integer> err = new Result.Err<>(404);
        assertTrue(err instanceof Result.Err);
        assertEquals(404, ((Result.Err<String, Integer>) err).error());
    }
    
    @Test
    void testScoreRecord() {
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 85);
        assertEquals("p1", record.playerId());
        assertEquals("Alice", record.playerName());
        assertEquals("g1", record.gameId());
        assertEquals("Chess", record.gameName());
        assertEquals(2, record.hoursPlayed());
        assertEquals(85, record.normalizedScore());
    }
    
    @Test
    void testPlayerAggregate() {
        PlayerAggregate aggregate = new PlayerAggregate("p1", "Alice", 170);
        assertEquals("p1", aggregate.playerId());
        assertEquals("Alice", aggregate.playerName());
        assertEquals(170, aggregate.totalScore());
    }
    
    @Test
    void testRankedResult() {
        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 200);
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 150);
        RankedResult result = new RankedResult(List.of(p1), List.of(p2));
        assertEquals(1, result.definiteWinners().size());
        assertEquals(1, result.tiedCandidates().size());
        assertEquals("p1", result.definiteWinners().get(0).playerId());
        assertEquals("p2", result.tiedCandidates().get(0).playerId());
    }
    
    @Test
    void testErrorRecords() {
        ParseError parseError = new ParseError("Invalid CSV format");
        assertEquals("Invalid CSV format", parseError.message());
        
        AggregationError aggregationError = new AggregationError("Duplicate player-game pair");
        assertEquals("Duplicate player-game pair", aggregationError.message());
        
        PipelineError pipelineError = new PipelineError("Pipeline failed");
        assertEquals("Pipeline failed", pipelineError.message());
    }
}