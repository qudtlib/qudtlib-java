package io.github.qudtlib.model;

/**
 * Represents a QUDT Quantity - A QuantityValue with a QuantityKind
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class Quantity {
    final QuantityValue quantityValue;

    final QuantityKind quantityKind;

    public Quantity(QuantityValue quantityValue, QuantityKind quantityKind) {
        this.quantityValue = quantityValue;
        this.quantityKind = quantityKind;
    }

    public QuantityValue getQuantityValue() {
        return quantityValue;
    }

    public QuantityKind getQuantityKind() {
        return quantityKind;
    }

    @Override
    public String toString() {
        return quantityKind.toString() + " of " + quantityValue.toString();
    }
}
