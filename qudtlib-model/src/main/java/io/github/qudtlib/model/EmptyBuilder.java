package io.github.qudtlib.model;

public class EmptyBuilder<T> implements Builder<T> {
    public EmptyBuilder() {}

    @Override
    public T build() {
        return null;
    }
}
