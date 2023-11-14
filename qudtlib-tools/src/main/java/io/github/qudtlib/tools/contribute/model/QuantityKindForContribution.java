package io.github.qudtlib.tools.contribute.model;

import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QuantityKind;

public class QuantityKindForContribution {
    private QuantityKind quantityKind;
    private QuantityKindMetadata quantityKindMetadata;

    public QuantityKindForContribution(
            QuantityKind quantityKind, QuantityKindMetadata quantityKindMetadata) {
        this.quantityKind = quantityKind;
        this.quantityKindMetadata = quantityKindMetadata;
    }

    public static Builder builder(String iri) {
        return new Builder(QuantityKind.definition(iri), QuantityKindMetadata.builder());
    }

    public static Builder builder(FactorUnits factorUnits, String localName) {
        return new Builder(
                QuantityKind.definition(factorUnits, localName), QuantityKindMetadata.builder());
    }

    public QuantityKind getQuantityKind() {
        return quantityKind;
    }

    public QuantityKindMetadata getQuantityKindMetadata() {
        return quantityKindMetadata;
    }

    public static class Builder {
        private QuantityKind.Definition quantityKindDefinition;
        private QuantityKindMetadata.Builder metadataBuilder;

        private Builder(
                QuantityKind.Definition quantityKindDefinition,
                QuantityKindMetadata.Builder metadataBuilder) {
            this.quantityKindDefinition = quantityKindDefinition;
            this.metadataBuilder = metadataBuilder;
        }

        public QuantityKindMetadata.Builder metadata() {
            return this.metadataBuilder;
        }

        public QuantityKind.Definition quantityKind() {
            return quantityKindDefinition;
        }

        public QuantityKindForContribution build() {
            return new QuantityKindForContribution(
                    this.quantityKindDefinition.build(), this.metadataBuilder.build());
        }
    }
}
