package io.github.qudtlib.tools.entitygen.support;

import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.QudtNamespaces;

public class QuantityKindBuilderFacade {
    private QuantityKind.Definition definition;

    public QuantityKindBuilderFacade(FactorUnits factorUnits, String localName) {
        this.definition =
                QuantityKind.definition(QudtNamespaces.quantityKind.makeIriInNamespace(localName));
        this.definition.dimensionVectorIri(factorUnits.getDimensionVectorIri());
    }

    public QuantityKindBuilderFacade addBroaderQuantityKind(QuantityKind quantityKind) {
        this.definition.addBroaderQuantityKind(QuantityKind.definition(quantityKind));
        return this;
    }

    public QuantityKindBuilderFacade label(String label, String languageTag) {
        this.definition.addLabel(label, languageTag);
        return this;
    }

    public QuantityKindBuilderFacade addExactMatch(QuantityKind quantityKind) {
        this.definition.addExactMatch(QuantityKind.definition(quantityKind));
        return this;
    }

    public QuantityKindBuilderFacade symbol(String symbol) {
        this.definition.symbol(symbol);
        return this;
    }

    public QuantityKind build() {
        return this.definition.build();
    }
}
