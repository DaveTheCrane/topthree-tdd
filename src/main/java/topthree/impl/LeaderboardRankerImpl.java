package topthree.impl;

import topthree.interfaces.LeaderboardRanker;
import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;
import java.util.List;

public class LeaderboardRankerImpl implements LeaderboardRanker {
    
    @Override
    public RankedResult rank(List<PlayerAggregate> aggregates) {
        return new RankedResult(List.of(), List.of());
    }
}