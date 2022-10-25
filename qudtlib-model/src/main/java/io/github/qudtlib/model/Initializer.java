package io.github.qudtlib.model;

import java.util.Map;
import java.util.Optional;

/**
 * Initializes the QUDTLib model.
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
            unit.getPrefixIri()
                    .ifPresent(
                            iri ->
                                    unit.setPrefix(
                                            Optional.ofNullable(prefixes.get(iri))
                                                    .orElseThrow(
                                                            () ->
                                                                    new IllegalStateException(
                                                                            String.format(
                                                                                    "Cannot setPrefix(Prefix) on unit %s: %s not found in model",
                                                                                    unit.getIri(),
                                                                                    iri)))));
            unit.getScalingOfIri()
                    .ifPresent(
                            iri ->
                                    unit.setScalingOf(
                                            Optional.ofNullable(units.get(iri))
                                                    .orElseThrow(
                                                            () ->
                                                                    new IllegalStateException(
                                                                            String.format(
                                                                                    "Cannot setScalingOf(Unit) on unit %s: %s not found in model",
                                                                                    unit.getIri(),
                                                                                    iri)))));
            unit.getQuantityKindIris()
                    .forEach(
                            iri ->
                                    unit.addQuantityKind(
                                            Optional.ofNullable(quantityKinds.get(iri))
                                                    .orElseThrow(
                                                            () ->
                                                                    new IllegalStateException(
                                                                            String.format(
                                                                                    "Cannot setQuantityKind(QuantityKind) on unit %s: %s not found in model",
                                                                                    unit.getIri(),
                                                                                    iri)))));
        }
    }

    Map<String, Unit> loadUnits();

    Map<String, QuantityKind> loadQuantityKinds();

    Map<String, Prefix> loadPrefixes();

    void loadFactorUnits(Map<String, Unit> units);
}
