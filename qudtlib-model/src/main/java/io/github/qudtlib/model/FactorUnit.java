package io.github.qudtlib.model;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static List<FactorUnit> contractExponents(List<FactorUnit> factorUnits) {
        return factorUnits.stream()
                .collect(groupingBy(FactorUnit::getKind, reducing((l, r) -> combine(l, r))))
                .values()
                .stream()
                .map(Optional::get)
                .collect(toList());
    }

    public static List<FactorUnit> reduceExponents(List<FactorUnit> factorUnits) {
        return factorUnits.stream()
                .collect(groupingBy(FactorUnit::getUnit, reducing((l, r) -> combine(l, r))))
                .values()
                .stream()
                .map(Optional::get)
                .filter(fu -> Math.abs(fu.getExponent()) > 0)
                .collect(toList());
    }

    public static FactorUnits normalizeFactorUnits(List<FactorUnit> factorUnits) {
        FactorUnits ret =
                factorUnits.stream()
                        .map(fu -> fu.normalize())
                        .reduce((prev, cur) -> cur.combineWith(prev))
                        .get();
        if (ret.isRatioOfSameUnits()) {
            return ret;
        }
        return ret.reduceExponents();
    }

    public static FactorUnit ofUnit(Unit unit) {
        return new FactorUnit(unit, 1);
    }

    public List<List<FactorUnit>> getAllPossibleFactorUnitCombinations() {
        List<List<FactorUnit>> subResult = this.unit.getAllPossibleFactorUnitCombinations();
        return subResult.stream()
                .map(fus -> fus.stream().map(fu -> fu.pow(this.exponent)).collect(toList()))
                .distinct()
                .collect(toList());
    }

    public static List<List<FactorUnit>> getAllPossibleFactorUnitCombinations(
            List<FactorUnit> factorUnits) {
        int numFactors = factorUnits.size();
        List<List<List<FactorUnit>>> subresults =
                factorUnits.stream()
                        .map(fu -> fu.getAllPossibleFactorUnitCombinations())
                        .collect(toList());
        int[] subResultLengths =
                subresults.stream().map(sr -> sr.size()).mapToInt(i -> i.intValue()).toArray();
        int[] currentIndices = new int[numFactors];
        List<List<FactorUnit>> results = new ArrayList<>();
        // cycle through all possible combinations of results per factor unit and combine them
        do {
            List<FactorUnit> curResult = new ArrayList<>();
            boolean countUp = true;
            for (int i = 0; i < numFactors; i++) {
                curResult.addAll(subresults.get(i).get(currentIndices[i]));
                if (countUp) {
                    currentIndices[i]++;
                    if (currentIndices[i] >= subResultLengths[i]) {
                        currentIndices[i] = 0;
                    } else {
                        countUp = false;
                    }
                }
            }
            addNoDuplicate(results, FactorUnit.contractExponents(curResult));
            addNoDuplicate(results, FactorUnit.reduceExponents(curResult));
        } while (IntStream.of(currentIndices).sum() > 0);
        return results;
    }

    private static void addNoDuplicate(List<List<FactorUnit>> list, List<FactorUnit> candidate) {
        if (!list.stream()
                .anyMatch(
                        elem ->
                                candidate.size() == elem.size()
                                        && elem.stream()
                                                .allMatch(
                                                        e ->
                                                                candidate.stream()
                                                                        .anyMatch(
                                                                                c ->
                                                                                        e.equals(
                                                                                                c))))) {
            list.add(candidate);
        }
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
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        if (!left.getUnit().equals(right.getUnit())) {
            throw new IllegalArgumentException(
                    "Cannot combine UnitFactors of different units (left: "
                            + left.getUnit()
                            + ", right: "
                            + right.getUnit()
                            + ")");
        }
        return new FactorUnit(left.getUnit(), left.getExponent() + right.getExponent());
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
                    .map(f -> f.pow(this.getExponent()))
                    .collect(Collectors.toList());
        }
        return List.of(this);
    }

    private FactorUnit withExponentMultiplied(int by) {
        return new FactorUnit(unit, this.exponent * by);
    }

    public FactorUnits normalize() {
        return this.unit.normalize().pow(this.exponent);
    }

    public FactorUnit pow(int exponent) {
        return new FactorUnit(this.unit, this.exponent * exponent);
    }
}
