package topthree.interfaces;

import topthree.models.PlayerAggregate;
import topthree.models.RankedResult;

import java.util.List;

public interface LeaderboardRanker {

    RankedResult rank(List<PlayerAggregate> aggregates);
}
