package com.topthree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ScoreAggregator for aggregating scores by player.
 */
public class ScoreAggregatorImpl implements ScoreAggregator {
    
    @Override
    public Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records) {
        if (records == null || records.isEmpty()) {
            return new Result.Ok<>(new ArrayList<>());
        }

        // Map player id to aggregation data: [playerName, totalScore]
        Map<String, String> playerNames = new LinkedHashMap<>();
        Map<String, Integer> totalScores = new LinkedHashMap<>();

        for (ScoreRecord record : records) {
            String playerId = record.player().playerId();
            String playerName = record.player().playerName();
            int weightedScore = record.gameEntry().hoursPlayed() * record.gameEntry().normalisedScore();

            // Update player name (last-seen wins)
            playerNames.put(playerId, playerName);
            
            // Accumulate weighted scores
            totalScores.put(playerId, totalScores.getOrDefault(playerId, 0) + weightedScore);
        }

        // Build result list maintaining order of first appearance
        List<PlayerAggregate> aggregates = records.stream()
            .map(r -> r.player().playerId())
            .distinct()
            .map(playerId -> new PlayerAggregate(
                new Player(playerId, playerNames.get(playerId)),
                totalScores.get(playerId)
            ))
            .collect(Collectors.toList());

        return new Result.Ok<>(aggregates);
    }
}
