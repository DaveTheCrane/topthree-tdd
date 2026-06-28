package com.topthree;

/**
 * Error type for pipeline failures.
 * @param message Description of the error
 * @param context Additional context about where the error occurred
 */
public record PipelineError(String message, String context) {}
