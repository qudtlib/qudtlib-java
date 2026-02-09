package io.github.qudtlib.model;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

    public static QuantityValue of(BigDecimal value, Unit unit) {
        return new QuantityValue(value, unit);
    }

    public static QuantityValue of(int value, Unit unit) {
        return new QuantityValue(new BigDecimal(value), unit);
    }

    public static QuantityValue of(float value, Unit unit) {
        return new QuantityValue(new BigDecimal(value), unit);
    }

    public static QuantityValue of(double value, Unit unit) {
        return new QuantityValue(new BigDecimal(value), unit);
    }

    public static QuantityValue of(long value, Unit unit) {
        return new QuantityValue(new BigDecimal(value), unit);
    }

    public static QuantityValue of(String value, Unit unit) {
        return new QuantityValue(new BigDecimal(value), unit);
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

    public QuantityValue convert(Unit toUnit) throws InconvertibleQuantitiesException {
        return convert(toUnit, null);
    }

    /**
     * @param toUnit
     * @param quantityKind optional quantity kind for handling edge cases
     * @return
     * @throws InconvertibleQuantitiesException
     */
    public QuantityValue convert(Unit toUnit, QuantityKind quantityKind)
            throws InconvertibleQuantitiesException {
        return new QuantityValue(this.unit.convert(this.value, toUnit, quantityKind), toUnit);
    }

    public QuantityValue subtract(QuantityValue subtrahend, MathContext mc) {
        return build(value.subtract(convertValueToUnit(subtrahend), mc));
    }

    public QuantityValue[] divideAndRemainder(QuantityValue divisor, MathContext mc) {
        BigDecimal[] components = value.divideAndRemainder(convertValueToUnit(divisor), mc);
        return new QuantityValue[] {build(components[0]), build(components[1])};
    }

    public int compareTo(QuantityValue val) {
        return value.compareTo(convertValueToUnit(val));
    }

    public QuantityValue divide(QuantityValue divisor) {
        return build(value.divide(convertValueToUnit(divisor)));
    }

    public QuantityValue pow(int n, MathContext mc) {
        return build(value.pow(n, mc));
    }

    public QuantityValue round(MathContext mc) {
        return build(value.round(mc));
    }

    public QuantityValue multiply(QuantityValue multiplicand, MathContext mc) {
        return build(value.multiply(convertValueToUnit(multiplicand), mc));
    }

    public QuantityValue setScale(int newScale) {
        return build(value.setScale(newScale));
    }

    public QuantityValue negate(MathContext mc) {
        return build(value.negate(mc));
    }

    public QuantityValue max(QuantityValue val) {
        return build(value.max(convertValueToUnit(val)));
    }

    public QuantityValue abs(MathContext mc) {
        return build(value.abs(mc));
    }

    public QuantityValue divideToIntegralValue(QuantityValue divisor) {
        return build(value.divideToIntegralValue(convertValueToUnit(divisor)));
    }

    public QuantityValue plus(MathContext mc) {
        return build(value.plus(mc));
    }

    public QuantityValue movePointRight(int n) {
        return build(value.movePointRight(n));
    }

    public QuantityValue remainder(QuantityValue divisor) {
        return build(value.remainder(convertValueToUnit(divisor)));
    }

    public QuantityValue add(QuantityValue augend, MathContext mc) {
        return build(value.add(convertValueToUnit(augend), mc));
    }

    public QuantityValue stripTrailingZeros() {
        return build(value.stripTrailingZeros());
    }

    public QuantityValue[] divideAndRemainder(QuantityValue divisor) {
        BigDecimal[] components = value.divideAndRemainder(convertValueToUnit(divisor));
        return new QuantityValue[] {build(components[0]), build(components[1])};
    }

    public QuantityValue pow(int n) {
        return build(value.pow(n));
    }

    public QuantityValue sqrt(MathContext mc) {
        return build(value.sqrt(mc));
    }

    public QuantityValue abs() {
        return build(value.abs());
    }

    public QuantityValue subtract(QuantityValue subtrahend) {
        return build(value.subtract(convertValueToUnit(subtrahend)));
    }

    public QuantityValue divide(QuantityValue divisor, RoundingMode roundingMode) {
        return build(value.divide(convertValueToUnit(divisor), roundingMode));
    }

    public QuantityValue setScale(int newScale, RoundingMode roundingMode) {
        return build(value.setScale(newScale, roundingMode));
    }

    public QuantityValue negate() {
        return build(value.negate());
    }

    public QuantityValue multiply(QuantityValue multiplicand) {
        return build(value.multiply(convertValueToUnit(multiplicand)));
    }

    public QuantityValue divide(QuantityValue divisor, MathContext mc) {
        return build(value.divide(convertValueToUnit(divisor), mc));
    }

    public QuantityValue min(QuantityValue val) {
        return build(value.min(convertValueToUnit(val)));
    }

    public QuantityValue add(QuantityValue augend) {
        return build(value.add(convertValueToUnit(augend)));
    }

    public QuantityValue divideToIntegralValue(QuantityValue divisor, MathContext mc) {
        return build(value.divideToIntegralValue(convertValueToUnit(divisor), mc));
    }

    public QuantityValue divide(QuantityValue divisor, int scale, RoundingMode roundingMode) {
        return build(value.divide(convertValueToUnit(divisor), scale, roundingMode));
    }

    public QuantityValue plus() {
        return build(value.plus());
    }

    public QuantityValue movePointLeft(int n) {
        return build(value.movePointLeft(n));
    }

    public QuantityValue remainder(QuantityValue divisor, MathContext mc) {
        return build(value.remainder(convertValueToUnit(divisor), mc));
    }

    public int signum() {
        return value.signum();
    }

    public QuantityValue scaleByPowerOfTen(int n) {
        return build(value.scaleByPowerOfTen(n));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    public String toString() {
        return value.toString() + " " + unit.toString();
    }

    private BigDecimal convertValueToUnit(QuantityValue subtrahend) {
        return subtrahend.getUnit().convert(subtrahend.getValue(), this.unit);
    }

    /**
     * Creates a new instance of {@code QuantityValue} with the specified value and the current
     * unit.
     *
     * @param value the numerical value for the new {@code QuantityValue}, represented as a {@code
     *     BigDecimal}
     * @return a new {@code QuantityValue} instance with the specified value and the current unit
     */
    private QuantityValue build(BigDecimal value) {
        return new QuantityValue(value, this.unit);
    }
}
