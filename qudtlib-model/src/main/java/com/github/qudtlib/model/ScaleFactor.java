package com.github.qudtlib.model;

import java.math.BigDecimal;

/**
 * A scale factor, used for the evaluation of queries for Units by factorUnits.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
class ScaleFactor {
    private final BigDecimal value;

    public ScaleFactor(BigDecimal value) {
        this.value = value;
    }

    public ScaleFactor() {
        this(BigDecimal.ONE);
    }

    public BigDecimal getValue() {
        return value;
    }

    public ScaleFactor multiplyBy(BigDecimal by) {
        return new ScaleFactor(this.value.multiply(by));
    }

    public ScaleFactor copy() {
        return new ScaleFactor(this.value);
    }

    @Override
    public String toString() {
        return "SF{" + value + '}';
    }
}
