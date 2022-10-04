package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Represents a part of a query by factor units while it is being evaluated. The <code>
 * factorUnitMatch</code> is <code>null</code> if no unit has been found to match yet, otherwise it
 * encapsulates the matched unit and accompanying data.
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

    /**
     * Creates a List of {@link FactorUnitSelector} objects that result from matching this
     * FactorUnitSelector with the specified {@link FactorUnit}. If the match is exact, the
     * resulting list contains only one object, if the exponent of the selector is higher than the
     * cumulative exponent of the <code>factorUnit</code>'s , a selector for the remaining power
     * (the selector's exponent minus the factorUnit's exponent) is added.
     *
     * <p>The <b>cumulative exponent</b> is the exponent obtained by following the path from the
     * top-level unit to the <code>factorUnit</code> we have found a match for.
     *
     * <p>For example, let the unit be <code>A-PER-J</code> and we have just found a match for
     * <code>M^-1</code>. What's the cumulative exponent of <code>M</code> in this case? <code>
     * A-PER-J = (A J^-1)</code>, and <code>J = (N M) = (KiloGM M^2 SEC^-2)</code>, so the path to
     * find <code>M</code> is <code>/A-PER-J/J/M or A-PER-J/J/N/M</code>. In each case, the
     * cumulative exponent of M is -1 because the exponent of J is -1. After matching one <code>M^-1
     * </code>, there is <code>(A M^-1 KiloGM^-^1 SEC^2)</code> left to be matched.
     *
     * <p>The <b>matchedMultiplier</b> is the factor needed to scale from this FactorUnitSelector's
     * unit to the <code>factorUnit</code> we are matching with it.
     *
     * <p>For example, if <code>this.unit</code> is <code>KiloM^2</code> and the <code>factorUnit
     * </code> is <code>M^2</code>, the <code>matchedMultiplier</code> is <code>(10^3)^2 = 10^6
     * </code>.
     *
     * @param factorUnit the matched unit
     * @param cumulativeExponent the
     * @param matchedPath the (inverted) list of Units traversed on the way here
     * @param scaleFactor the scale factor
     * @return the list of FactorUnitSelectors resulting from matching this FactorUnitSelector in
     *     the specified situation
     */
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
        Optional<BigDecimal> matchedMultiplier =
                calculateMatchedMultiplier(factorUnit, matchedPower);
        if (matchedMultiplier.isEmpty()) {
            throw new IllegalArgumentException("units do not match");
        }
        int remainingPower = this.exponent - matchedPower;
        List<FactorUnitSelector> ret = new ArrayList<>();
        ret.add(
                matched(
                        new FactorUnitMatch(
                                factorUnit, matchedMultiplier.get(), matchedPath, scaleFactor)));
        if (remainingPower != 0) {
            ret.add(new FactorUnitSelector(this.unit, remainingPower));
        }
        return ret;
    }

    private Optional<BigDecimal> calculateMatchedMultiplier(
            FactorUnit factorUnit, int matchedExponent) {
        Objects.requireNonNull(factorUnit);
        if (!this.unit.isConvertible(factorUnit.getUnit())) {
            return Optional.empty();
        }
        BigDecimal conversionMultiplier = factorUnit.getUnit().getConversionMultiplier(this.unit);
        return Optional.of(conversionMultiplier.pow(matchedExponent, MathContext.DECIMAL128));
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
        return ""
                + unit
                + (exponent == 1 ? "" : "^" + exponent)
                + "@"
                + ((factorUnitMatch == null) ? "?" : factorUnitMatch);
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
