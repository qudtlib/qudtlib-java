package io.github.qudtlib.nodedef;

public interface SettableBuilder<T> extends Builder<T> {
    void setProduct(T product);
}
