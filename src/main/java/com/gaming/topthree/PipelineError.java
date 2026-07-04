package com.gaming.topthree;

/**
 * Describes a failure during an end-to-end pipeline run.
 */
public record PipelineError(String message, String context) {}
