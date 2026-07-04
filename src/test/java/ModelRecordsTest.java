// TDD Red-Green-Refactor Test
// Task 2.1.1 Red: Data model records exist and can be instantiated

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelRecordsTest {

    @Test
    void playerRecordExists() {
        Player player = new Player("p1", "Alice");
        assertNotNull(player);
        assertNotNull(player.id());
        assertNotNull(player.name());
    }

    @Test
    void gameEntryRecordExists() {
        GameEntry game = new GameEntry("g1", "Chess");
        assertNotNull(game);
        assertNotNull(game.id());
        assertNotNull(game.name());
    }

    @Test
    void scoreRecordRecordExists() {
        ScoreRecord score = new ScoreRecord("p1", "Alice", "g1", "Chess", 10, 85);
        assertNotNull(score);
        assertNotNull(score.playerId());
        assertNotNull(score.playerName());
        assertNotNull(score.gameId());
        assertNotNull(score.gameName());
        assertNotNull(score.hoursPlayed());
        assertNotNull(score.normalisedScore());
    }

    @Test
    void playerAggregateRecordExists() {
        PlayerAggregate aggregate = new PlayerAggregate("p1", "Alice", 100);
        assertNotNull(aggregate);
        assertNotNull(aggregate.playerId());
        assertNotNull(aggregate.playerName());
        assertNotNull(aggregate.totalScore());
    }

    @Test
    void rankedResultRecordExists() {
        RankedResult result = new RankedResult(List.of(), List.of());
        assertNotNull(result);
        assertNotNull(result.definiteWinners());
        assertNotNull(result.tiedCandidates());
    }

    @Test
    void parseErrorRecordExists() {
        ParseError error = new ParseError("Invalid input");
        assertNotNull(error);
        assertNotNull(error.message());
    }

    @Test
    void aggregationErrorRecordExists() {
        AggregationError error = new AggregationError("Failed to aggregate");
        assertNotNull(error);
        assertNotNull(error.message());
    }

    @Test
    void pipelineErrorRecordExists() {
        PipelineError error = new PipelineError("Pipeline failed");
        assertNotNull(error);
        assertNotNull(error.message());
    }
}
