package com.topthree;

/**
 * Represents an error that occurred in the pipeline processing.
 *
 * @param message Descriptive error message
 * @param context Additional context about the error (e.g., offending line, duplicate pair)
 */
public record PipelineError(String message, String context) {}
