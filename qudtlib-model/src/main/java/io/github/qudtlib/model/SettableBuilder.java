package io.github.qudtlib.model;

public interface SettableBuilder<T> extends Builder<T> {
    void setProduct(T product);
}
