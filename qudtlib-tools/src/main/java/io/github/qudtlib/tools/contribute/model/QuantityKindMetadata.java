package io.github.qudtlib.tools.contribute.model;

public class QuantityKindMetadata extends CommonEntityMetadata {
    public QuantityKindMetadata() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CommonEntityMetadata.Builder<QuantityKindMetadata> {
        public Builder() {
            super(new QuantityKindMetadata());
        }
    }
}
