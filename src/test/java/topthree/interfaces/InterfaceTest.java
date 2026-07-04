package topthree.interfaces;

import topthree.models.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InterfaceTest {
    
    @Test
    void testCsvParserInterface() {
        CsvParser parser = new CsvParser() {
            @Override
            public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
                return new Result.Err<>(new ParseError("Not implemented"));
            }
            
            @Override
            public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
                return new Result.Err<>(new ParseError("Not implemented"));
            }
        };
        
        Result<ScoreRecord, ParseError> result = parser.parseLine("test");
        assertTrue(result instanceof Result.Err);
    }
    
    @Test
    void testPrettyPrinterInterface() {
        PrettyPrinter printer = new PrettyPrinter() {
            @Override
            public String print(ScoreRecord record) {
                return "Not implemented";
            }
        };
        
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 2, 85);
        assertEquals("Not implemented", printer.print(record));
    }
    
    @Test
    void testScoreAggregatorInterface() {
        ScoreAggregator aggregator = new ScoreAggregator() {
            @Override
            public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
                return new Result.Err<>(new AggregationError("Not implemented"));
            }
        };
        
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());
        assertTrue(result instanceof Result.Err);
    }
    
    @Test
    void testLeaderboardRankerInterface() {
        LeaderboardRanker ranker = new LeaderboardRanker() {
            @Override
            public RankedResult rank(List<PlayerAggregate> aggregates) {
                return new RankedResult(List.of(), List.of());
            }
        };
        
        RankedResult result = ranker.rank(List.of());
        assertNotNull(result);
        assertTrue(result.definiteWinners().isEmpty());
        assertTrue(result.tiedCandidates().isEmpty());
    }
    
    @Test
    void testTopThreePipelineInterface() {
        TopThreePipeline pipeline = new TopThreePipeline() {
            @Override
            public Result<RankedResult, PipelineError> run(List<String> csvLines) {
                return new Result.Err<>(new PipelineError("Not implemented"));
            }
        };
        
        Result<RankedResult, PipelineError> result = pipeline.run(List.of());
        assertTrue(result instanceof Result.Err);
    }
}