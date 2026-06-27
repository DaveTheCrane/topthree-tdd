package com.topthree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreAggregatorTest {

    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();

    @Test
    void emptyInputReturnsEmptyList() {
        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of());

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertTrue(aggregates.isEmpty());
    }

    @Test
    void singleRecordProducesOnePlayerAggregateWithCorrectTotalScore() {
        Player player = new Player("p1", "Alice");
        GameEntry game = new GameEntry("g1", "Chess", 2, 50);
        ScoreRecord record = new ScoreRecord(player, game);

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        assertEquals(new Player("p1", "Alice"), aggregate.player());
        assertEquals(100, aggregate.totalScore());
    }

    @Test
    void twoRecordsForDifferentPlayersProduceTwoPlayerAggregates() {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        ScoreRecord record1 = new ScoreRecord(p1, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(p2, new GameEntry("g2", "Poker", 3, 30));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(2, aggregates.size());
        assertEquals(new Player("p1", "Alice"), aggregates.get(0).player());
        assertEquals(new Player("p2", "Bob"), aggregates.get(1).player());
    }

    @Test
    void samePlayerIdWithDifferentDisplayNamesLastSeenNameWins() {
        Player firstEncounter = new Player("p1", "Alice");
        Player secondEncounter = new Player("p1", "Alicia");
        ScoreRecord record1 = new ScoreRecord(firstEncounter, new GameEntry("g1", "Chess", 2, 50));
        ScoreRecord record2 = new ScoreRecord(secondEncounter, new GameEntry("g2", "Poker", 3, 30));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alicia", aggregates.get(0).player().playerName());
    }

    @Test
    void playerDisplayNameIsPreservedInPlayerAggregate() {
        Player player = new Player("p1", "Alice");
        ScoreRecord record = new ScoreRecord(player, new GameEntry("g1", "Chess", 2, 50));

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        assertEquals("Alice", aggregates.get(0).player().playerName());
    }

    @Test
    void twoRecordsForSamePlayerSumTheirWeightedScores() {
        Player player = new Player("p1", "Alice");
        ScoreRecord record1 = new ScoreRecord(player, new GameEntry("g1", "Chess", 2, 50));   // weighted = 100
        ScoreRecord record2 = new ScoreRecord(player, new GameEntry("g2", "Poker", 3, 30));   // weighted = 90

        Result<List<PlayerAggregate>, AggregationError> result = aggregator.aggregate(List.of(record1, record2));

        assertInstanceOf(Result.Ok.class, result);
        List<PlayerAggregate> aggregates = ((Result.Ok<List<PlayerAggregate>, AggregationError>) result).value();
        assertEquals(1, aggregates.size());
        PlayerAggregate aggregate = aggregates.get(0);
        assertEquals(new Player("p1", "Alice"), aggregate.player());
        assertEquals(190, aggregate.totalScore());
    }
}
