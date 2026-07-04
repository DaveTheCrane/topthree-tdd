package com.gaming.topthree;

/**
 * Describes a failure to parse a CSV line.
 */
public record ParseError(String message, String offendingLine) {}
