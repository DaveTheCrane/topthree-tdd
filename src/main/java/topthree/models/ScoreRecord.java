package topthree.models;

public record ScoreRecord(
    String playerId,
    String playerName,
    String gameId,
    String gameName,
    int hoursPlayed,
    int normalizedScore
) {}