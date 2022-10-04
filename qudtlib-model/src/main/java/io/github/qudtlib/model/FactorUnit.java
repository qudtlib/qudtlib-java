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
            ScaleFactor scaleFactor) {
        Set<FactorUnitSelection> mySelection = new HashSet<>(selection);
        // descend into unit
        mySelection =
                this.unit.match(
                        mySelection,
                        getExponentCumulated(cumulativeExponent),
                        matchedPath,
                        scaleFactor);
        return matchNotRecursingIntoUnit(mySelection, cumulativeExponent, matchedPath, scaleFactor);
    }

    public Set<FactorUnitSelection> matchNotRecursingIntoUnit(
            Set<FactorUnitSelection> selection,
            int cumulativeExponent,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        // now match this one
        Set<FactorUnitSelection> ret = new HashSet<>();
        for (FactorUnitSelection factorUnitSelection : selection) {
            // add one solution where this node is matched
            FactorUnitSelection processedSelection =
                    factorUnitSelection.forPotentialMatch(
                            this, cumulativeExponent, matchedPath, scaleFactor);
            if (!processedSelection.equals(factorUnitSelection)) {
                // if there was a match, (i.e, we modified the selection),
                // it's a new partial solution - return it
                ret.add(processedSelection);
            }
            // also regard the selection without the match as a possible partial solution
            ret.add(factorUnitSelection);
        }
        // lower level
        return ret;
    }

    boolean isMatched(FactorUnitSelection selection, Deque<Unit> checkedPath) {
        if (isMatchedNoRecursingIntoUnit(selection, checkedPath)) {
            return true;
        }
        return unit.isMatched(selection, checkedPath);
    }

    public boolean isMatchedNoRecursingIntoUnit(
            FactorUnitSelection selection, Deque<Unit> checkedPath) {
        return selection.isSelected(this, checkedPath);
    }

    @Override
    public String toString() {
        return unit + "^" + exponent;
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
