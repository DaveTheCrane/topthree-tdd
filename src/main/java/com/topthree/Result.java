package com.topthree;

/**
 * A discriminated union type representing either a success value (Ok) or an error (Err).
 * Used to propagate errors without crossing component boundaries via exceptions.
 *
 * @param <V> The type of the success value
 * @param <E> The type of the error
 */
public sealed interface Result<V, E> permits Result.Ok, Result.Err {
    /**
     * Success case containing a value.
     */
    record Ok<V, E>(V value) implements Result<V, E> {}

    /**
     * Error case containing an error.
     */
    record Err<V, E>(E error) implements Result<V, E> {}
}
