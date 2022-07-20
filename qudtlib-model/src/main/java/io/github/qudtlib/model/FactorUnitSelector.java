package io.github.qudtlib.model;

import static java.util.stream.Collectors.joining;

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
    private final FactorUnit matchedFactorUnit;
    private final List<Unit> matchedPath;
    private final BigDecimal matchedMultiplier;
    private final ScaleFactor scaleFactor;

    public FactorUnitSelector(Unit unit, int exponent) {
        this.unit = unit;
        this.exponent = exponent;
        this.matchedFactorUnit = null;
        this.matchedMultiplier = null;
        this.scaleFactor = null;
        this.matchedPath = null;
    }

    public FactorUnitSelector(
            Unit unit,
            int exponent,
            FactorUnit matchedFactorUnit,
            BigDecimal matchedMultiplier,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        this.unit = unit;
        this.exponent = exponent;
        this.matchedFactorUnit = matchedFactorUnit;
        this.matchedPath = matchedPath != null ? new ArrayList<>(matchedPath) : null;
        this.matchedMultiplier = matchedMultiplier;
        this.scaleFactor = scaleFactor;
    }

    public FactorUnitSelector(
            Unit unit,
            int exponent,
            FactorUnit matchedFactorUnit,
            BigDecimal matchedMultiplier,
            List<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        this.unit = unit;
        this.exponent = exponent;
        this.matchedFactorUnit = matchedFactorUnit;
        this.matchedPath = matchedPath != null ? new ArrayList<>(matchedPath) : null;
        this.matchedMultiplier = matchedMultiplier;
        this.scaleFactor = scaleFactor;
    }

    public FactorUnitSelector matched(
            FactorUnit matchedFactorUnit,
            BigDecimal matchedMultiplier,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        Objects.requireNonNull(matchedFactorUnit);
        Objects.requireNonNull(matchedMultiplier);
        Objects.requireNonNull(scaleFactor);
        return new FactorUnitSelector(
                this.unit,
                matchedFactorUnit.getExponent(),
                matchedFactorUnit,
                matchedMultiplier,
                matchedPath,
                scaleFactor);
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
            throw new IllegalArgumentException("epxonents must match");
        }
        int matchedPower = factorUnit.getExponentCumulated(cumulativeExponent);
        BigDecimal matchedMultiplier = calculateMatchedMultiplier(factorUnit, matchedPower);
        if (matchedMultiplier == null) {
            throw new IllegalArgumentException("units must match");
        }
        int remainingPower = this.exponent - matchedPower;
        List<FactorUnitSelector> ret = new ArrayList<>();
        ret.add(matched(factorUnit, matchedMultiplier, matchedPath, scaleFactor));
        if (remainingPower != 0) {
            ret.add(new FactorUnitSelector(this.unit, remainingPower));
        }
        return ret;
    }

    boolean isScalable(Unit unit) {
        return unit.getPrefix().isPresent() && unit.getScalingOf().isPresent();
    }

    private BigDecimal calculateMatchedMultiplier(FactorUnit factorUnit, int matchedExponent) {
        if (this.unit.equals(factorUnit.unit)) {
            return BigDecimal.ONE;
        }
        if (isScalable(factorUnit.unit)
                && isScalable(this.unit)
                && factorUnit
                        .getUnit()
                        .getScalingOf()
                        .map(u -> this.unit.getScalingOf().map(u2 -> u2.equals(u)).orElse(false))
                        .orElse(false)) {
            return factorUnit
                    .getUnit()
                    .getPrefix()
                    .orElseThrow(() -> new IllegalStateException("Prefix required!"))
                    .getMultiplier()
                    .pow(matchedExponent, MathContext.DECIMAL128)
                    .multiply(
                            this.unit
                                    .getPrefix()
                                    .orElseThrow(
                                            () -> new IllegalStateException("Prefix required!"))
                                    .getMultiplier()
                                    .pow(-matchedExponent, MathContext.DECIMAL128));
        }
        if (isScalable(this.unit)
                && this.unit
                        .getScalingOf()
                        .map(u -> u.equals(factorUnit.getUnit()))
                        .orElse(false)) {
            return this.unit
                    .getPrefix()
                    .orElseThrow(() -> new IllegalStateException("Prefix required!"))
                    .getMultiplier()
                    .pow(-matchedExponent, MathContext.DECIMAL128);
        }
        if (isScalable(factorUnit.unit)
                && factorUnit
                        .getUnit()
                        .getScalingOf()
                        .map(u -> u.equals(this.unit))
                        .orElse(false)) {
            return factorUnit
                    .getUnit()
                    .getPrefix()
                    .orElseThrow(() -> new IllegalStateException("Prefix required!"))
                    .getMultiplier()
                    .pow(matchedExponent, MathContext.DECIMAL128);
        }

        return null;
    }

    private boolean exponentMatches(FactorUnit factorUnit, int cumulativeExponent) {
        int cumulatedFactorUnitExponent = factorUnit.getExponentCumulated(cumulativeExponent);
        return Math.abs(this.exponent) > 0
                && Math.abs(this.exponent) >= Math.abs(cumulatedFactorUnitExponent)
                && Integer.signum(this.exponent) == Integer.signum(cumulatedFactorUnitExponent);
    }

    private boolean unitMatches(FactorUnit factorUnit) {
        if (this.unit.equals(factorUnit.unit)) {
            return true;
        }
        if (isScalable(this.unit)
                && this.unit
                        .getScalingOf()
                        .map(u -> u.equals(factorUnit.getUnit()))
                        .orElse(false)) {
            return true;
        }
        if (isScalable(factorUnit.unit)
                && factorUnit
                        .getUnit()
                        .getScalingOf()
                        .map(u -> u.equals(this.unit))
                        .orElse(false)) {
            return true;
        }
        return isScalable(factorUnit.unit)
                && isScalable(this.unit)
                && factorUnit
                        .getUnit()
                        .getScalingOf()
                        .map(u -> this.unit.getScalingOf().map(u2 -> u2.equals(u)).orElse(false))
                        .orElse(false);
    }

    public boolean matches(FactorUnit factorUnit, int cumulativeExponent) {
        return exponentMatches(factorUnit, cumulativeExponent) && unitMatches(factorUnit);
    }

    public FactorUnitSelector copy() {
        return new FactorUnitSelector(
                this.unit,
                this.exponent,
                this.matchedFactorUnit,
                this.matchedMultiplier,
                this.matchedPath,
                this.scaleFactor);
    }

    public Unit getUnit() {
        return unit;
    }

    public int getExponent() {
        return exponent;
    }

    public FactorUnit getMatchedFactorUnit() {
        return matchedFactorUnit;
    }

    public BigDecimal getMatchedMultiplier() {
        return matchedMultiplier;
    }

    public List<Unit> getMatchedPath() {
        return matchedPath;
    }

    public ScaleFactor getScaleFactor() {
        return scaleFactor;
    }

    public boolean isAvailable() {
        return matchedFactorUnit == null;
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
                + ((matchedFactorUnit == null)
                        ? "(not matched)"
                        : ("(at "
                                + Optional.ofNullable(matchedPath).orElse(List.of()).stream()
                                        .map(Object::toString)
                                        .collect(joining("/"))
                                + ", MM{"
                                + matchedMultiplier
                                + "}, "
                                + scaleFactor
                                + ")"))
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnitSelector selector = (FactorUnitSelector) o;
        return exponent == selector.exponent
                && unit.equals(selector.unit)
                && Objects.equals(matchedFactorUnit, selector.matchedFactorUnit)
                && Objects.equals(matchedPath, selector.matchedPath)
                && ((matchedMultiplier == null && selector.matchedMultiplier == null)
                        || (matchedMultiplier != null && selector.matchedMultiplier != null)
                                && matchedMultiplier.compareTo(selector.matchedMultiplier) == 0)
                && Objects.equals(scaleFactor, selector.scaleFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                unit, exponent, matchedFactorUnit, matchedPath, matchedMultiplier, scaleFactor);
    }
}
