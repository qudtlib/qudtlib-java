package io.github.qudtlib.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuantityValueTests {

    private Unit meter;
    private Unit centimeter;
    private Unit kilogram;
    private QuantityValue twoMeters;
    private QuantityValue fiveCentimeters;

    @BeforeEach
    void setUp() {
        // Stub Units with conversion logic. Assuming Unit has convert method.
        // For testing, we'll create anonymous subclasses or stubs.
        DimensionVector dvLength = DimensionVector.builder().length(1).build();
        DimensionVector dvMass = DimensionVector.builder().mass(1).build();
        meter =
                Unit.definition("Meter")
                        .conversionMultiplier(BigDecimal.ONE)
                        .symbol("m")
                        .dimensionVectorIri(dvLength.getDimensionVectorIri())
                        .build();
        centimeter =
                Unit.definition("CentiMeter")
                        .conversionMultiplier(new BigDecimal("0.01"))
                        .dimensionVectorIri(dvLength.getDimensionVectorIri())
                        .scalingOf(meter)
                        .symbol("cm")
                        .build();
        kilogram =
                Unit.definition("KiloGram")
                        .conversionMultiplier(BigDecimal.ONE)
                        .dimensionVectorIri(dvMass.getDimensionVectorIri())
                        .symbol("kg")
                        .build();
        twoMeters = meter.quantityValue(2);
        fiveCentimeters = centimeter.quantityValue(5);
    }

    @Test
    void testConstructorAndGetters() {
        QuantityValue qv = new QuantityValue(BigDecimal.TEN, meter);
        assertThat(qv.getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(qv.getUnit()).isEqualTo(meter);
    }

    @Test
    void testEqualsAndHashCode() {
        QuantityValue qv1 = new QuantityValue(BigDecimal.TEN, meter);
        QuantityValue qv2 = new QuantityValue(BigDecimal.TEN, meter);
        QuantityValue qv3 = new QuantityValue(BigDecimal.ONE, meter);
        assertThat(qv1).isEqualTo(qv2);
        assertThat(qv1.hashCode()).isEqualTo(qv2.hashCode());
        assertThat(qv1).isNotEqualTo(qv3);
    }

    @Test
    void testConvert() throws InconvertibleQuantitiesException {
        QuantityValue converted = twoMeters.convert(centimeter);
        assertThat(converted.getValue()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(converted.getUnit()).isEqualTo(centimeter);
    }

    @Test
    void testConvertWithQuantityKind() throws InconvertibleQuantitiesException {
        QuantityKind length = QuantityKind.definition("Length").build();
        QuantityValue converted = twoMeters.convert(centimeter, length);
        assertThat(converted.getValue()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void testConvertInconvertibleThrowsException() {
        Unit incompatibleUnit = kilogram; // Different dimension
        assertThatThrownBy(() -> twoMeters.convert(incompatibleUnit))
                .isInstanceOf(InconvertibleQuantitiesException.class);
    }

    @Test
    void testAdd() {
        QuantityValue result = twoMeters.add(fiveCentimeters);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2.05"));
        assertThat(result.getUnit()).isEqualTo(meter);
    }

    @Test
    void testAddWithMathContext() {
        MathContext mc = new MathContext(2, RoundingMode.HALF_UP);
        QuantityValue result = twoMeters.add(fiveCentimeters, mc);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2.1")); // Rounded
    }

    @Test
    void testSubtract() {
        QuantityValue result = twoMeters.subtract(fiveCentimeters);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("1.95"));
    }

    @Test
    void testMultiply() {
        QuantityValue multiplicand = new QuantityValue(new BigDecimal("3"), meter);
        QuantityValue result = twoMeters.multiply(multiplicand);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("6"));
        // Note: Units would become m^2, but since QuantityValue doesn't handle unit multiplication,
        // assume same unit for test
    }

    @Test
    void testDivide() {
        QuantityValue divisor = new QuantityValue(new BigDecimal("2"), meter);
        QuantityValue result = twoMeters.divide(divisor);
        assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void testDivideWithScaleAndRounding() {
        QuantityValue divisor = new QuantityValue(new BigDecimal("3"), meter);
        QuantityValue result = twoMeters.divide(divisor, 2, RoundingMode.HALF_UP);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("0.67"));
    }

    @Test
    void testPow() {
        QuantityValue result = twoMeters.pow(2);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("4"));
    }

    @Test
    void testSqrt() {
        MathContext mc = MathContext.DECIMAL64;
        QuantityValue fourMeters = new QuantityValue(new BigDecimal("4"), meter);
        QuantityValue result = fourMeters.sqrt(mc);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void testAbs() {
        QuantityValue negative = new QuantityValue(new BigDecimal("-2"), meter);
        QuantityValue result = negative.abs();
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void testNegate() {
        QuantityValue result = twoMeters.negate();
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("-2"));
    }

    @Test
    void testMinAndMax() {
        assertThat(twoMeters.min(fiveCentimeters).getValue())
                .isEqualByComparingTo(new BigDecimal("0.05")); // Converted
        assertThat(twoMeters.max(fiveCentimeters).getValue())
                .isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void testRemainder() {
        QuantityValue divisor = new QuantityValue(new BigDecimal("1.5"), meter);
        QuantityValue result = twoMeters.remainder(divisor);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    void testDivideAndRemainder() {
        QuantityValue divisor = new QuantityValue(new BigDecimal("1.5"), meter);
        QuantityValue[] results = twoMeters.divideAndRemainder(divisor);
        assertThat(results[0].getValue()).isEqualByComparingTo(new BigDecimal("1"));
        assertThat(results[1].getValue()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    void testSignum() {
        assertThat(twoMeters.signum()).isEqualTo(1);
        assertThat(new QuantityValue(BigDecimal.ZERO, meter).signum()).isEqualTo(0);
        assertThat(new QuantityValue(new BigDecimal("-1"), meter).signum()).isEqualTo(-1);
    }

    @Test
    void testScaleByPowerOfTen() {
        QuantityValue result = twoMeters.scaleByPowerOfTen(2);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void testSetScale() {
        QuantityValue result =
                new QuantityValue(new BigDecimal("2.12345"), meter)
                        .setScale(2, RoundingMode.HALF_UP);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2.12"));
    }

    @Test
    void testStripTrailingZeros() {
        QuantityValue result =
                new QuantityValue(new BigDecimal("2.000"), meter).stripTrailingZeros();
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void testMovePointLeftAndRight() {
        assertThat(twoMeters.movePointLeft(1).getValue())
                .isEqualByComparingTo(new BigDecimal("0.2"));
        assertThat(twoMeters.movePointRight(1).getValue())
                .isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    void testCompareTo() {
        assertThat(twoMeters.compareTo(fiveCentimeters)).isPositive();
        assertThat(fiveCentimeters.compareTo(twoMeters)).isNegative();
        assertThat(twoMeters.compareTo(new QuantityValue(new BigDecimal("2"), meter))).isZero();
    }

    @Test
    void testToString() {
        assertThat(twoMeters.toString()).isEqualTo("2 m");
    }

    // Edge cases
    @Test
    void testZeroValueOperations() {
        QuantityValue zero = new QuantityValue(BigDecimal.ZERO, meter);
        assertThat(zero.add(twoMeters)).isEqualTo(twoMeters);
        assertThat(zero.divide(twoMeters, RoundingMode.HALF_UP)).isEqualTo(meter.quantityValue(0));
    }

    @Test
    void testNegativeValues() {
        QuantityValue negative = new QuantityValue(new BigDecimal("-2"), meter);
        assertThat(negative.add(twoMeters).getValue()).isZero();
    }

    @Test
    void testPrecisionLossWithMathContext() {
        MathContext mc = new MathContext(1);
        QuantityValue result = twoMeters.divide(new QuantityValue(new BigDecimal("3"), meter), mc);
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("0.7")); // Rounded
    }
}
