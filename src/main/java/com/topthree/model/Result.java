package com.topthree.model;

public sealed interface Result<V, E> permits Result.Ok, Result.Err {
    record Ok<V, E>(V value) implements Result<V, E> {
    }

    record Err<V, E>(E error) implements Result<V, E> {
    }

    static <V, E> Result<V, E> ok(V value) {
        return new Ok<>(value);
    }

    static <V, E> Result<V, E> err(E error) {
        return new Err<>(error);
    }
}
