package io.github.qudtlib.model;

import static java.util.stream.Collectors.*;

import io.github.qudtlib.nodedef.NodeDefinition;
import io.github.qudtlib.nodedef.SettableBuilderBase;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Combines a {@link Unit} and an exponent; some Units are a combination of {@link FactorUnit}s. If
 * a unit is such a 'derived unit', its {@link Unit#getFactorUnits()} method returns a non-empty Set
 * of FactorUnits.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class FactorUnit {

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FactorUnit factorUnit) {
        return new Builder(factorUnit);
    }

    public static class Builder extends SettableBuilderBase<FactorUnit> {
        private Integer exponent;
        private io.github.qudtlib.nodedef.Builder<Unit> unitBuilder;

        public Builder() {}

        public Builder(FactorUnit presetProduct) {
            super(presetProduct);
        }

        public Builder exponent(int exponent) {
            this.exponent = exponent;
            resetProduct();
            return this;
        }

        public Builder unit(NodeDefinition<String, Unit> unitDefinition) {
            this.unitBuilder = unitDefinition;
            resetProduct();
            return this;
        }

        public Builder unit(Unit unit) {
            Objects.requireNonNull(unit);
            this.unitBuilder = Unit.definition(unit);
            return this;
        }

        public FactorUnit doBuild() {
            return new FactorUnit(this);
        }
    }

    final int exponent;
    final Unit unit;

    private FactorUnit(Builder builder) {
        Objects.requireNonNull(builder.unitBuilder);
        Objects.requireNonNull(builder.exponent);
        this.exponent = builder.exponent;
        this.unit = builder.unitBuilder.build();
    }

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
        return FactorUnit.builder().unit(unit).exponent(1).build();
    }

    public BigDecimal conversionMultiplier() {
        return this.getUnit()
                .getConversionMultiplier()
                .orElse(BigDecimal.ONE)
                .pow(this.exponent, MathContext.DECIMAL128);
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
        return FactorUnit.builder()
                .unit(left.getUnit())
                .exponent(left.getExponent() + right.getExponent())
                .build();
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
        if (FactorUnits.hasFactorUnits(leafFactorUnits)) {
            return leafFactorUnits.stream()
                    .map(f -> f.pow(this.getExponent()))
                    .collect(Collectors.toList());
        }
        return List.of(this);
    }

    public Stream<FactorUnit> streamLeafFactorUnitsWithCumulativeExponents() {
        List<FactorUnit> leafFactorUnits = this.unit.getLeafFactorUnitsWithCumulativeExponents();
        if (FactorUnits.hasFactorUnits(leafFactorUnits)) {
            return leafFactorUnits.stream().map(f -> f.pow(this.getExponent()));
        }
        return Stream.of(this);
    }

    private FactorUnit withExponentMultiplied(int by) {
        return FactorUnit.builder().unit(unit).exponent(this.exponent * by).build();
    }

    public FactorUnits normalize() {
        return this.unit.normalize().pow(this.exponent);
    }

    public FactorUnit pow(int exponent) {
        return new FactorUnit(this.unit, this.exponent * exponent);
    }

    public Optional<String> getDimensionVectorIri() {
        if (this.unit.getDimensionVectorIri().isEmpty()) {
            return Optional.empty();
        }
        DimensionVector dv = new DimensionVector(this.unit.getDimensionVectorIri().get());
        return Optional.of(dv.multiply(this.exponent).getDimensionVectorIri());
    }
}
