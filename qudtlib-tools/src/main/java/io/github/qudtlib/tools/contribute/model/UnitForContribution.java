package io.github.qudtlib.tools.contribute.model;

import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.support.ContributionHelper;

public class UnitForContribution {

    private UnitMetadata metadata;
    private Unit unit;

    public UnitForContribution(Unit unit, UnitMetadata metadata) {
        this.metadata = metadata;
        this.unit = unit;
    }

    public UnitMetadata getMetadata() {
        return metadata;
    }

    public Unit getUnit() {
        return unit;
    }

    public static Builder builder(String iri) {
        return new Builder(iri);
    }

    public static Builder builder(FactorUnits factorUnits) {
        return new Builder(factorUnits);
    }

    public static class Builder {
        private Unit.Definition unitDefinition;
        private UnitMetadata.Builder metadataBuilder;

        private Builder(Unit.Definition unitDefinition, UnitMetadata.Builder metadataBuilder) {
            this.unitDefinition = unitDefinition;
            this.metadataBuilder = metadataBuilder;
        }

        public Builder(String iri) {
            this(Unit.definition(iri), UnitMetadata.builder());
        }

        public Builder(FactorUnits factorUnits) {
            this(ContributionHelper.derivedUnitDefinition(factorUnits), UnitMetadata.builder());
        }

        public Unit.Definition unit() {
            return this.unitDefinition;
        }

        public UnitMetadata.Builder metadata() {
            return this.metadataBuilder;
        }

        public UnitForContribution build() {
            return new UnitForContribution(
                    this.unitDefinition.build(), this.metadataBuilder.build());
        }
    }
}
