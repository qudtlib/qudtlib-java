package io.github.qudtlib.nodedef;

public interface NodeDefinition<I, T> extends Builder<T> {
    I getId();
}
