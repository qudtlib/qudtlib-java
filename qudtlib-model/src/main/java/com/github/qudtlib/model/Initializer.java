package com.github.qudtlib.model;

import java.util.Map;

/**
 * Initializes the QUDTLib model in {@link Qudt}'s static initializer.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public interface Initializer {
    default void connectObjects(
            Map<String, Unit> units,
            Map<String, QuantityKind> quantityKinds,
            Map<String, Prefix> prefixes) {
        for (Unit unit : units.values()) {
            unit.getPrefixIri().ifPresent(iri -> unit.setPrefix(prefixes.get(iri)));
            unit.getScalingOfIri().ifPresent(iri -> unit.setScalingOf(units.get(iri)));
            unit.getQuantityKindIris().forEach(iri -> unit.addQuantityKind(quantityKinds.get(iri)));
        }
    }

    Map<String, Unit> loadUnits();

    Map<String, QuantityKind> loadQuantityKinds();

    Map<String, Prefix> loadPrefixes();

    void loadFactorUnits(Map<String, Unit> units);
}
