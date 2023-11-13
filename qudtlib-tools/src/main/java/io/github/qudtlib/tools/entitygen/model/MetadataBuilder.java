package io.github.qudtlib.tools.entitygen.model;

public abstract class MetadataBuilder<T extends CommonEntityMetadata> {
    protected T product;

    public MetadataBuilder(T product) {
        this.product = product;
    }

    public T build() {
        return product;
    }
}
