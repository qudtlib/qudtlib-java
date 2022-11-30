package io.github.qudtlib.model;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FactorUnits {
    private List<FactorUnit> factorUnits;
    private BigDecimal scaleFactor = new BigDecimal("1");

    public FactorUnits(List<FactorUnit> factorUnits, BigDecimal scaleFactor) {
        this.factorUnits = factorUnits;
        this.scaleFactor = scaleFactor;
    }

    public FactorUnits(List<FactorUnit> factorUnits) {
        this.factorUnits = factorUnits;
    }

    public static FactorUnits ofUnit(Unit unit) {
        return new FactorUnits(List.of(new FactorUnit(unit, 1)));
    }

    /**
     * Accepts up to 7 pairs of &lt;Unit, Integer&gt; which are interpreted as factor units and
     * respective exponents.
     *
     * @param factorUnitSpec array of up to 7 %lt;Unit, Integer%gt; pairs
     * @return true if the specified unit/exponent combination identifies this unit.
     *     (overspecification is counted as a match)
     */
    public static FactorUnits ofFactorUnitSpec(Object... factorUnitSpec) {
        if (factorUnitSpec.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of arguments is required");
        }
        if (factorUnitSpec.length > 14) {
            throw new IllegalArgumentException(
                    "No more than 14 arguments (7 factor units) supported");
        }
        List<FactorUnit> factorUnits = new ArrayList<>();
        for (int i = 0; i < factorUnitSpec.length; i += 2) {
            Unit requestedUnit;
            requestedUnit = ((Unit) factorUnitSpec[i]);
            Integer requestedExponent = (Integer) factorUnitSpec[i + 1];
            factorUnits.add(new FactorUnit(requestedUnit, requestedExponent));
        }
        return new FactorUnits(factorUnits);
    }

    public static FactorUnits ofFactorUnitSpec(
            Collection<Map.Entry<String, Integer>> factorUnitSpec) {
        Object[] arr = new Object[factorUnitSpec.size() * 2];
        return FactorUnits.ofFactorUnitSpec(
                factorUnitSpec.stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(toList())
                        .toArray(arr));
    }

    public List<FactorUnit> getFactorUnits() {
        return factorUnits;
    }

    public BigDecimal getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnits that = (FactorUnits) o;

        return scaleFactor.compareTo(that.scaleFactor) == 0
                && new HashSet<>(factorUnits).equals(new HashSet<>(that.factorUnits));
    }

    @Override
    public int hashCode() {
        return Objects.hash(factorUnits, scaleFactor);
    }

    @Override
    public String toString() {
        return (this.scaleFactor.compareTo(BigDecimal.ONE) == 0
                        ? ""
                        : this.scaleFactor.toString() + "*")
                + factorUnits;
    }

    public FactorUnits pow(int exponent) {
        return new FactorUnits(
                this.factorUnits.stream().map(fu -> fu.pow(exponent)).collect(toList()),
                this.scaleFactor.pow(exponent, MathContext.DECIMAL128));
    }

    public FactorUnits combineWith(FactorUnits other) {
        if (other == null) {
            return this;
        }
        return new FactorUnits(
                FactorUnit.contractExponents(
                        Stream.concat(this.factorUnits.stream(), other.factorUnits.stream())
                                .collect(Collectors.toList())),
                this.scaleFactor.multiply(other.scaleFactor, MathContext.DECIMAL128));
    }

    public boolean isRatioOfSameUnits() {
        return this.factorUnits.size() == 2
                && this.factorUnits.get(0).getUnit().equals(this.factorUnits.get(1).getUnit())
                && this.factorUnits.get(0).getExponent() == this.factorUnits.get(1).getExponent();
    }

    public FactorUnits reduceExponents() {
        return new FactorUnits(FactorUnit.reduceExponents(this.factorUnits), this.scaleFactor);
    }

    public FactorUnits scale(BigDecimal by) {
        return new FactorUnits(this.factorUnits, this.scaleFactor.multiply(by));
    }

    public FactorUnits normalize() {
        FactorUnits normalized = FactorUnit.normalizeFactorUnits(this.factorUnits);
        return new FactorUnits(
                normalized.getFactorUnits(),
                normalized.getScaleFactor().multiply(this.scaleFactor));
    }
}
