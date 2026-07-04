package topthree.models;

public sealed interface Result<V, E> permits Result.Ok, Result.Err {

    boolean isOk();

    record Ok<V, E>(V value) implements Result<V, E> {
        public boolean isOk() {
            return true;
        }
    }

    record Err<V, E>(E error) implements Result<V, E> {
        public boolean isOk() {
            return false;
        }
    }
}
