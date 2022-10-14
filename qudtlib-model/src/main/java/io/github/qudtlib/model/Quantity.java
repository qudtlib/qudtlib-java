package io.github.qudtlib.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a QUDT Quantity - a set of {@link QuantityValue}s.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class Quantity {
    final Set<QuantityValue> quantityValues;

    public Quantity(Set<QuantityValue> quantityValues) {
        this.quantityValues = quantityValues;
    }

    public Set<QuantityValue> getQuantityValues() {
        return Collections.unmodifiableSet(quantityValues);
    }

    @Override
    public String toString() {
        return "Quantity{"
                + quantityValues.stream().map(Objects::toString).collect(Collectors.joining(", "))
                + '}';
    }
}
