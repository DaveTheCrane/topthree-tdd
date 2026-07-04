package com.gaming.topthree;

/**
 * Describes a failure during score aggregation. Reserved for future error
 * conditions; name conflicts are resolved by last-write-wins.
 */
public record AggregationError(String message, String playerId) {}
