package topthree.models;

import java.util.List;

public record RankedResult(
    List<PlayerAggregate> definiteWinners,
    List<PlayerAggregate> tiedCandidates
) {}