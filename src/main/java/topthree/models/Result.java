package topthree.models;

public sealed interface Result<V, E> permits Result.Ok, Result.Err {
    
    record Ok<V, E>(V value) implements Result<V, E> {}
    
    record Err<V, E>(E error) implements Result<V, E> {}
}