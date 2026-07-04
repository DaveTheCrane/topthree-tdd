import org.junit.jupiter.api.Test;
import topthree.impl.CsvParserImpl;
import topthree.impl.LeaderboardRankerImpl;
import topthree.impl.ScoreAggregatorImpl;
import topthree.impl.TopThreePipelineImpl;
import topthree.interfaces.TopThreePipeline;
import topthree.models.PipelineError;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;
import topthree.models.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Feature: top-three-high-scores
class PipelineTest {

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        TopThreePipeline pipeline = new TopThreePipelineImpl(
                new CsvParserImpl(),
                new ScoreAggregatorImpl(),
                new LeaderboardRankerImpl()
        );

        Result<RankedResult, PipelineError> result = pipeline.run(List.of());

        assertTrue(result.isOk());
        RankedResult ranked = result.get();
        assertEquals(List.of(), ranked.definiteWinners());
        assertEquals(List.of(), ranked.tiedCandidates());
    }

    @Test
    void validCsvListProducesCorrectRankedResult() {
        TopThreePipeline pipeline = new TopThreePipelineImpl(
                new CsvParserImpl(),
                new ScoreAggregatorImpl(),
                new LeaderboardRankerImpl()
        );

        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,2,50",
                "p2,Bob,g2,Poker,3,40",
                "p3,Carol,g3,Racing,1,80"
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertTrue(result.isOk());
        RankedResult ranked = result.get();
        // p1: 2*50=100, p2: 3*40=120, p3: 1*80=80
        // Top 3: Bob(120), Alice(100), Carol(80)
        assertEquals(3, ranked.definiteWinners().size());
        assertEquals("p2", ranked.definiteWinners().get(0).playerId());
        assertEquals("p1", ranked.definiteWinners().get(1).playerId());
        assertEquals("p3", ranked.definiteWinners().get(2).playerId());
    }

    @Test
    void invalidCsvLineReturnsPipelineError() {
        TopThreePipeline pipeline = new TopThreePipelineImpl(
                new CsvParserImpl(),
                new ScoreAggregatorImpl(),
                new LeaderboardRankerImpl()
        );

        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,2,50",
                "p2,Bob"  // Invalid line
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertFalse(result.isOk());
        assertEquals("CSV parse error: Expected 6 fields, got 2", result.error().message());
    }

    @Test
    void duplicatePlayerIdGameIdPairReturnsPipelineError() {
        TopThreePipeline pipeline = new TopThreePipelineImpl(
                new CsvParserImpl(),
                new ScoreAggregatorImpl(),
                new LeaderboardRankerImpl()
        );

        List<String> csvLines = List.of(
                "p1,Alice,g1,Chess,2,50",
                "p1,Alice,g1,Chess,3,60"  // Duplicate
        );

        Result<RankedResult, PipelineError> result = pipeline.run(csvLines);

        assertFalse(result.isOk());
        assertEquals("Duplicate playerId/gameId pair: p1:g1", result.error().message());
    }
}
