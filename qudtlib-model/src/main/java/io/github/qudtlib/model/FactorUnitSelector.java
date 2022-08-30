package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Represents a part of a query by factor units while it is being evaluated.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
class FactorUnitSelector {
    private final Unit unit;
    private final int exponent;
    private final FactorUnitMatch factorUnitMatch;

    public FactorUnitSelector(Unit unit, int exponent) {
        this.unit = unit;
        this.exponent = exponent;
        this.factorUnitMatch = null;
    }

    public FactorUnitSelector(Unit unit, int exponent, FactorUnitMatch factorUnitMatch) {
        this.unit = unit;
        this.exponent = exponent;
        this.factorUnitMatch = factorUnitMatch;
    }

    public FactorUnitSelector matched(FactorUnitMatch factorUnitMatch) {
        Objects.requireNonNull(factorUnitMatch);
        return new FactorUnitSelector(this.unit, this.exponent, factorUnitMatch);
    }

    public List<FactorUnitSelector> forMatch(
            FactorUnit factorUnit,
            int cumulativeExponent,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        if (!this.isAvailable()) {
            throw new IllegalArgumentException("not available - selector is already bound");
        }
        if (!exponentMatches(factorUnit, cumulativeExponent)) {
            throw new IllegalArgumentException("epxonents do not match");
        }
        int matchedPower = factorUnit.getExponentCumulated(cumulativeExponent);
        BigDecimal matchedMultiplier = calculateMatchedMultiplier(factorUnit, matchedPower);
        if (matchedMultiplier == null) {
            throw new IllegalArgumentException("units do not match");
        }
        int remainingPower = this.exponent - matchedPower;
        List<FactorUnitSelector> ret = new ArrayList<>();
        ret.add(
                matched(
                        new FactorUnitMatch(
                                factorUnit, matchedMultiplier, matchedPath, scaleFactor)));
        if (remainingPower != 0) {
            ret.add(new FactorUnitSelector(this.unit, remainingPower));
        }
        return ret;
    }

    private BigDecimal calculateMatchedMultiplier(FactorUnit factorUnit, int matchedExponent) {
        Objects.requireNonNull(factorUnit);
        if (!this.unit.isConvertible(factorUnit.getUnit())) {
            return null;
        }
        BigDecimal conversionMultiplier = factorUnit.getUnit().getConversionMultiplier(this.unit);
        return conversionMultiplier.pow(matchedExponent, MathContext.DECIMAL128);
    }

    private boolean exponentMatches(FactorUnit factorUnit, int cumulativeExponent) {
        int cumulatedFactorUnitExponent = factorUnit.getExponentCumulated(cumulativeExponent);
        return Math.abs(this.exponent) > 0
                && Math.abs(this.exponent) >= Math.abs(cumulatedFactorUnitExponent)
                && Integer.signum(this.exponent) == Integer.signum(cumulatedFactorUnitExponent);
    }

    public boolean matches(FactorUnit factorUnit, int cumulativeExponent) {
        return exponentMatches(factorUnit, cumulativeExponent)
                && this.unit.isSameScaleAs(factorUnit.getUnit());
    }

    public FactorUnitSelector copy() {
        return new FactorUnitSelector(this.unit, this.exponent, this.factorUnitMatch);
    }

    public Unit getUnit() {
        return unit;
    }

    public int getExponent() {
        return exponent;
    }

    public Optional<FactorUnitMatch> getFactorUnitMatch() {
        return Optional.ofNullable(this.factorUnitMatch);
    }

    public boolean isAvailable() {
        return factorUnitMatch == null;
    }

    public boolean isBound() {
        return !isAvailable();
    }

    @Override
    public String toString() {
        return "FUS{"
                + unit
                + "^"
                + exponent
                + ((factorUnitMatch == null) ? ",(not matched)" : "," + factorUnitMatch)
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnitSelector selector = (FactorUnitSelector) o;
        return exponent == selector.exponent
                && unit.equals(selector.unit)
                && Objects.equals(factorUnitMatch, selector.factorUnitMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, exponent, factorUnitMatch);
    }
}
