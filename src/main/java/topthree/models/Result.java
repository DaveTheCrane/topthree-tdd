package topthree.models;

public sealed interface Result<V, E> permits Result.Ok, Result.Err {

    boolean isOk();

    V get();

    E error();

    record Ok<V, E>(V value) implements Result<V, E> {
        public boolean isOk() {
            return true;
        }

        public V get() {
            return value;
        }

        public E error() {
            throw new IllegalStateException("Cannot get error from Ok");
        }
    }

    record Err<V, E>(E error) implements Result<V, E> {
        public boolean isOk() {
            return false;
        }

        public V get() {
            throw new IllegalStateException("Cannot get value from Err");
        }

        public E error() {
            return error;
        }
    }
}
