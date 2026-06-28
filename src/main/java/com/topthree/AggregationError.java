package com.topthree;

/**
 * Error type for score aggregation failures.
 * @param message Description of the error
 * @param playerId The player id associated with the error (if applicable)
 */
public record AggregationError(String message, String playerId) {}
