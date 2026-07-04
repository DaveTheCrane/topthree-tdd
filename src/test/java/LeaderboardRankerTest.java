import org.junit.jupiter.api.Test;
import topthree.impl.LeaderboardRankerImpl;
import topthree.interfaces.LeaderboardRanker;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Feature: top-three-high-scores
class LeaderboardRankerTest {

    @Test
    void emptyInputReturnsEmptyRankedResult() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        RankedResult result = ranker.rank(List.of());

        assertEquals(new RankedResult(List.of(), List.of()), result);
    }

    @Test
    void singlePlayerAndTwoPlayersAllInDefiniteWinners() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        // Single player
        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 100);
        RankedResult result1 = ranker.rank(List.of(p1));
        assertEquals(List.of(p1), result1.definiteWinners());
        assertEquals(List.of(), result1.tiedCandidates());

        // Two players
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 200);
        RankedResult result2 = ranker.rank(List.of(p2, p1));
        assertEquals(List.of(p2, p1), result2.definiteWinners());
        assertEquals(List.of(), result2.tiedCandidates());
    }

    @Test
    void threeDistinctScoresAllInDefiniteWinners() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 300);
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 200);
        PlayerAggregate p3 = new PlayerAggregate("p3", "Carol", 100);

        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void fourPlayersNoTieTopThreeInDefiniteWinners() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 400);
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 300);
        PlayerAggregate p3 = new PlayerAggregate("p3", "Carol", 200);
        PlayerAggregate p4 = new PlayerAggregate("p4", "Dave", 100);

        RankedResult result = ranker.rank(List.of(p1, p2, p3, p4));

        assertEquals(List.of(p1, p2, p3), result.definiteWinners());
        assertEquals(List.of(), result.tiedCandidates());
    }

    @Test
    void tieAtPosition3SplitDefiniteWinnersAndTiedCandidates() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 400);
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 300);
        PlayerAggregate p3 = new PlayerAggregate("p3", "Carol", 200);
        PlayerAggregate p4 = new PlayerAggregate("p4", "Dave", 200);

        RankedResult result = ranker.rank(List.of(p1, p2, p3, p4));

        assertEquals(List.of(p1, p2), result.definiteWinners());
        assertEquals(List.of(p3, p4), result.tiedCandidates());
    }

    @Test
    void allPlayersTiedDefiniteWinnersEmptyAllInTiedCandidates() {
        LeaderboardRanker ranker = new LeaderboardRankerImpl();

        PlayerAggregate p1 = new PlayerAggregate("p1", "Alice", 100);
        PlayerAggregate p2 = new PlayerAggregate("p2", "Bob", 100);
        PlayerAggregate p3 = new PlayerAggregate("p3", "Carol", 100);

        RankedResult result = ranker.rank(List.of(p1, p2, p3));

        assertEquals(List.of(), result.definiteWinners());
        assertEquals(List.of(p1, p2, p3), result.tiedCandidates());
    }
}
