package io.github.qudtlib.nodedef;

public class EmptyBuilder<T> implements Builder<T> {
    public EmptyBuilder() {}

    @Override
    public T build() {
        return null;
    }
}
