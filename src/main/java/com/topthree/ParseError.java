package com.topthree;

/**
 * Represents an error during CSV parsing.
 *
 * @param message       Descriptive error message
 * @param offendingLine The CSV line that caused the error
 */
public record ParseError(String message, String offendingLine) {}
