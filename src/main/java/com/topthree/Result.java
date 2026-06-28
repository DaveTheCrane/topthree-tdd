package com.topthree;

import java.util.NoSuchElementException;

/**
 * A discriminated result type (Either-style) that is either a success value (Ok) or an error (Err).
 */
public sealed interface Result<V, E> permits Result.Ok, Result.Err {

    /**
     * Success variant containing a value.
     */
    record Ok<V, E>(V value) implements Result<V, E> {}

    /**
     * Error variant containing an error.
     */
    record Err<V, E>(E error) implements Result<V, E> {}

    /**
     * Creates a success result with the given value.
     */
    static <V, E> Result<V, E> ok(V value) {
        return new Ok<>(value);
    }

    /**
     * Creates an error result with the given error.
     */
    static <V, E> Result<V, E> err(E error) {
        return new Err<>(error);
    }

    /**
     * Checks if this is an Ok result.
     */
    default boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * Checks if this is an Err result.
     */
    default boolean isErr() {
        return this instanceof Err;
    }

    /**
     * Returns the value if this is Ok, throws NoSuchElementException otherwise.
     */
    default V get() {
        if (this instanceof Ok<V, E> ok) {
            return ok.value;
        }
        throw new NoSuchElementException("Cannot get value from Err");
    }

    /**
     * Returns the error if this is Err, throws NoSuchElementException otherwise.
     */
    default E getError() {
        if (this instanceof Err<V, E> err) {
            return err.error;
        }
        throw new NoSuchElementException("Cannot get error from Ok");
    }
}
