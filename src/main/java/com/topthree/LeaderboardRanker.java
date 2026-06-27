package com.topthree;

import java.util.List;

public interface LeaderboardRanker {
    RankedResult rank(List<PlayerAggregate> aggregates);
}
