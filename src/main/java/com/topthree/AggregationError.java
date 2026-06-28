package com.topthree;

/**
 * Represents an error during score aggregation.
 *
 * @param message  Descriptive error message
 * @param playerId The player id associated with the error
 */
public record AggregationError(String message, String playerId) {}
