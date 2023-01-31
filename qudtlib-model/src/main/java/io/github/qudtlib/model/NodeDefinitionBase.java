package io.github.qudtlib.model;

import java.util.Objects;

public abstract class NodeDefinitionBase<I, T extends SelfSmuggler> extends SettableBuilderBase<T>
        implements NodeDefinition<I, T> {
    final I id;

    public NodeDefinitionBase(I id, T presetProduct) {
        super(presetProduct);
        Objects.requireNonNull(id);
        this.id = id;
    }

    public NodeDefinitionBase(I id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    @Override
    public final T build() {
        if (this.getProduct() == null) {
            T built = doBuild();
            // the product may have been set by a SelfSmuggler. In that case, don't overwrite it.
            if (this.getProduct() == null) {
                this.setProduct(built);
            }
        }
        return getProduct();
    }

    public I getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeDefinitionBase)) return false;
        NodeDefinitionBase<?, ?> that = (NodeDefinitionBase<?, ?>) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
