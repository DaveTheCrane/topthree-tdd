package topthree.impl;

import topthree.interfaces.LeaderboardRanker;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardRankerImpl implements LeaderboardRanker {

    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        if (aggregates.isEmpty()) {
            return new RankedResult(List.of(), List.of());
        }

        List<PlayerAggregate> sorted = new ArrayList<>(aggregates);
        sorted.sort(Comparator.comparingInt(PlayerAggregate::totalScore).reversed());

        // If 3 or fewer players, check if they're all tied
        if (sorted.size() <= 3) {
            int firstScore = sorted.get(0).totalScore();
            boolean allTied = sorted.stream().allMatch(p -> p.totalScore() == firstScore);
            if (allTied && sorted.size() == 3) {
                // 3 players all tied - no one is definitely in top 3
                return new RankedResult(List.of(), sorted);
            }
            return new RankedResult(sorted, List.of());
        }

        // Get the score at position 3 (index 2, which is the 3rd place)
        int scoreAtPos3 = sorted.get(2).totalScore();
        // Get the score at position 4 (index 3, which is the player just after top 3)
        int scoreAtPos4 = sorted.get(3).totalScore();

        // Case: Tie at boundary (3rd place score equals 4th place score)
        // This means the 3rd place is shared, so we can't definitively pick top 3
        if (scoreAtPos3 == scoreAtPos4) {
            List<PlayerAggregate> definiteWinners = new ArrayList<>();
            List<PlayerAggregate> tiedCandidates = new ArrayList<>();
            for (PlayerAggregate aggregate : sorted) {
                if (aggregate.totalScore() > scoreAtPos3) {
                    definiteWinners.add(aggregate);
                } else {
                    tiedCandidates.add(aggregate);
                }
            }
            return new RankedResult(definiteWinners, tiedCandidates);
        }

        // Case: No tie at boundary - top 3 are definite winners
        List<PlayerAggregate> definiteWinners = new ArrayList<>(sorted.subList(0, 3));
        List<PlayerAggregate> tiedCandidates = new ArrayList<>();
        return new RankedResult(definiteWinners, tiedCandidates);
    }
}
