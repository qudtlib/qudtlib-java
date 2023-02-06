package io.github.qudtlib.nodedef;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class MapBackedNodeDefinition<K, T> implements NodeDefinition<K, T> {

    private final Map<K, ? extends NodeDefinition<K, T>> builders;
    private final K key;
    private final Supplier<? extends RuntimeException> notFoundExceptionSupplier;

    public MapBackedNodeDefinition(
            Map<K, ? extends NodeDefinition<K, T>> builders,
            K key,
            Supplier<? extends RuntimeException> notFoundExceptionSupplier) {
        Objects.requireNonNull(builders);
        Objects.requireNonNull(key);
        this.builders = builders;
        this.key = key;
        this.notFoundExceptionSupplier =
                notFoundExceptionSupplier != null
                        ? notFoundExceptionSupplier
                        : () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Expected entity %s not found", key.toString()));
    }

    @Override
    public T build() {
        return Optional.ofNullable(this.builders.get(this.key))
                .orElseThrow(this.notFoundExceptionSupplier)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDefinition<?, ?> that = (NodeDefinition<?, ?>) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public K getId() {
        return key;
    }
}
