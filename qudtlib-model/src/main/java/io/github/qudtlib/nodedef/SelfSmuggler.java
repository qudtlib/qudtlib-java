package io.github.qudtlib.nodedef;

public class SelfSmuggler {
    public <B extends SettableBuilder<T>, T extends SelfSmuggler> SelfSmuggler(B builder) {
        builder.setProduct((T) this);
    }
}
