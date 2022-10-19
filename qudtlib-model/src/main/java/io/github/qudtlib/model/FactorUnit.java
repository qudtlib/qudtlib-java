package io.github.qudtlib.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combines a {@link Unit} and an exponent; some Units are a combination of {@link FactorUnit}s. If
 * a unit is such a 'derived unit', its {@link Unit#getFactorUnits()} method returns a non-empty Set
 * of FactorUnits.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class FactorUnit {
    final int exponent;
    final Unit unit;

    public FactorUnit(Unit unit, int exponent) {
        Objects.requireNonNull(unit);
        this.exponent = exponent;
        this.unit = unit;
    }

    public String getKind() {
        return unit.getIri() + " " + Integer.signum(exponent);
    }

    public int getExponent() {
        return exponent;
    }

    public int getExponentCumulated(int cumulatedExponent) {
        return exponent * cumulatedExponent;
    }

    public Unit getUnit() {
        return unit;
    }

    public static FactorUnit combine(FactorUnit left, FactorUnit right) {
        if (!left.getKind().equals(right.getKind())) {
            throw new IllegalArgumentException(
                    "Cannot combine UnitFactors of different kind (left: "
                            + left.getKind()
                            + ", right: "
                            + right.getKind()
                            + ")");
        }
        return new FactorUnit(left.getUnit(), left.getExponent() + right.getExponent());
    }

    Set<FactorUnitSelection> match(
            Set<FactorUnitSelection> selection,
            int cumulativeExponent,
            Deque<Unit> matchedPath,
            FactorUnitMatchingMode mode) {
        Set<FactorUnitSelection> mySelection = new HashSet<>(selection);
        // descend into unit, with cumulated exponent
        return this.unit.match(
                mySelection, getExponentCumulated(cumulativeExponent), matchedPath, mode);
    }

    @Override
    public String toString() {
        return unit + (exponent == 1 ? "" : "^" + exponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnit that = (FactorUnit) o;
        return exponent == that.exponent && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exponent, unit);
    }

    public List<FactorUnit> getLeafFactorUnitsWithCumulativeExponents() {
        List<FactorUnit> leafFactorUnits = this.unit.getLeafFactorUnitsWithCumulativeExponents();
        if (!leafFactorUnits.isEmpty()) {
            return leafFactorUnits.stream()
                    .map(f -> f.withExponentMultiplied(this.getExponent()))
                    .collect(Collectors.toList());
        }
        return List.of(this);
    }

    private FactorUnit withExponentMultiplied(int by) {
        return new FactorUnit(unit, this.exponent * by);
    }
}
