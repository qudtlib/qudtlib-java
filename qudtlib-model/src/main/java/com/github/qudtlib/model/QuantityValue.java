package com.github.qudtlib.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a QUDT QuantityValue, ie. the combination of a {@link BigDecimal} value and a {@link
 * Unit}.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QuantityValue {
    private final BigDecimal value;
    private final Unit unit;

    public QuantityValue(BigDecimal value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuantityValue that = (QuantityValue) o;
        return value.compareTo(that.value) == 0 && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    public String toString() {
        return value.toString() + unit.toString();
    }
}
