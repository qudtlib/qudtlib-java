package io.github.qudtlib.nodedef;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class SettableBuilderBase<T> implements SettableBuilder<T> {

    private volatile T product;

    public SettableBuilderBase(T presetProduct) {
        Objects.requireNonNull(presetProduct);
        this.product = presetProduct;
    }

    public SettableBuilderBase() {}

    protected void resetProduct() {
        this.product = null;
    }

    @Override
    public void setProduct(T product) {
        this.product = product;
    }

    protected abstract T doBuild();

    protected T getProduct() {
        return product;
    }

    protected <T> void doIfPresent(T val, Consumer<T> setter) {
        if (val != null) {
            setter.accept(val);
        }
    }

    @Override
    public T build() {
        if (this.product == null) {
            this.product = doBuild();
        }
        return product;
    }
}
