package com.topthree;

/**
 * Error type for CSV parsing failures.
 * @param message Description of the error
 * @param offendingLine The CSV line that caused the error
 */
public record ParseError(String message, String offendingLine) {}
