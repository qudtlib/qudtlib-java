package io.github.qudtlib.model;

public class SelfSmuggler {
    public <B extends SettableBuilder<T>, T extends SelfSmuggler> SelfSmuggler(B builder) {
        builder.setProduct((T) this);
    }
}
