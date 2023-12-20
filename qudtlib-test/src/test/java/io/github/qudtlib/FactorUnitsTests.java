package io.github.qudtlib;

import static io.github.qudtlib.model.Units.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FactorUnitsTests {
    @ParameterizedTest
    @MethodSource
    public void testNormalize(FactorUnits factorUnitsToNormalize, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnitsToNormalize.normalize());
    }

    public static Stream<Arguments> testNormalize() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 2),
                        FactorUnits.ofFactorUnitSpec(Units.M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1).getFactorUnits(),
                                BigDecimal.valueOf(1E3))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 2),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 2).getFactorUnits(),
                                BigDecimal.valueOf(1E6))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 2, KiloGM, -2),
                        FactorUnits.ofFactorUnitSpec(GM, 2, GM, -2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, Units.M, -3),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E3))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, Units.M3, -1),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E3))));
    }

    @ParameterizedTest
    @MethodSource
    public void testPow(FactorUnits factorUnits, int power, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnits.pow(power));
    }

    public static Stream<Arguments> testPow() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 2),
                        1,
                        FactorUnits.ofFactorUnitSpec(Units.M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), Units.M, 2),
                        1,
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), Units.M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1),
                        -3,
                        FactorUnits.ofFactorUnitSpec(KiloGM, -3)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), KiloGM, 1),
                        -3,
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(0.000000001), KiloGM, -3)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1), 0, FactorUnits.ofFactorUnitSpec()),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 2),
                        3,
                        FactorUnits.ofFactorUnitSpec(KiloGM, 6)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), KiloGM, 2),
                        3,
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000000000), KiloGM, 6)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.W, 1, Units.HR, 1, Units.M, -2),
                        -1,
                        FactorUnits.ofFactorUnitSpec(Units.W, -1, Units.HR, -1, Units.M, 2)));
    }

    @ParameterizedTest
    @MethodSource
    public void testCombineWith(
            FactorUnits factorUnits, FactorUnits other, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnits.combineWith(other));
    }

    public static Stream<Arguments> testCombineWith() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2),
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1),
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, Units.M, -2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2),
                        null,
                        FactorUnits.ofFactorUnitSpec(Units.M, -2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1),
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, Units.SEC, -2),
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, KiloGM, 1, Units.SEC, -2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), Units.M, 1),
                        FactorUnits.ofFactorUnitSpec(
                                BigDecimal.valueOf(1000), KiloGM, 1, Units.SEC, -2),
                        FactorUnits.ofFactorUnitSpec(
                                BigDecimal.valueOf(1000000),
                                Units.M,
                                1,
                                KiloGM,
                                1,
                                Units.SEC,
                                -2)));
    }

    @ParameterizedTest
    @MethodSource
    public void testIsRatioOfSameUnits(FactorUnits factorUnits, boolean expectedResult) {
        assertEquals(expectedResult, factorUnits.isRatioOfSameUnits());
    }

    public static Stream<Arguments> testIsRatioOfSameUnits() {
        return Stream.of(
                Arguments.of(FactorUnits.ofFactorUnitSpec(Units.M, -2), false),
                Arguments.of(FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1), false),
                Arguments.of(FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, -1), true),
                Arguments.of(FactorUnits.ofFactorUnitSpec(Units.N, 1, Units.N, -1), true));
    }

    @ParameterizedTest
    @MethodSource
    public void testReduceExponents(FactorUnits factorUnits, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnits.reduceExponents());
    }

    public static Stream<Arguments> testReduceExponents() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        FactorUnits.ofFactorUnitSpec()),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, -1),
                        FactorUnits.ofFactorUnitSpec(Units.M, -3)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1),
                        FactorUnits.ofFactorUnitSpec(Units.M, 2)));
    }

    @ParameterizedTest
    @MethodSource
    public void testConversionFactor(
            FactorUnits factorUnits, Unit toUnit, BigDecimal expectedResult) {
        MatcherAssert.assertThat(
                factorUnits.conversionFactor(toUnit), Matchers.comparesEqualTo(expectedResult));
    }

    public static Stream<Arguments> testConversionFactor() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 2),
                        Units.FT2,
                        new BigDecimal("10.76391041670972230833350555590000")),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, Units.MilliSEC, -1),
                        Units.GM__PER__DAY,
                        new BigDecimal("86400000000.00000000000000000000003")),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(GM, 1, Units.DAY, -1),
                        Units.KiloGM__PER__SEC,
                        new BigDecimal("0.00000001157407407407407407407407407407407")));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDimensionVectorIri(FactorUnits factorUnits, String expectedResult) {
        assertEquals(expectedResult, factorUnits.getDimensionVectorIri());
    }

    public static Stream<Arguments> testGetDimensionVectorIri() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(M2, 1, M, -1),
                        "http://qudt.org/vocab/dimensionvector/A0E0L1I0M0H0T0D0"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(M2, 1, M, -2),
                        "http://qudt.org/vocab/dimensionvector/A0E0L0I0M0H0T0D1"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(RAD, 1, SEC, -1),
                        "http://qudt.org/vocab/dimensionvector/A0E0L0I0M0H0T-1D0"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(RAD, 1),
                        "http://qudt.org/vocab/dimensionvector/A0E0L0I0M0H0T0D1"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), RAD, 1),
                        "http://qudt.org/vocab/dimensionvector/A0E0L0I0M0H0T0D1"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(
                                BigDecimal.valueOf(1000), MOL__PER__M2__SEC__M__SR, -2),
                        "http://qudt.org/vocab/dimensionvector/A-2E0L6I0M0H0T2D0"));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetLocalName(FactorUnits factorUnits, String expectedResult) {
        assertEquals(expectedResult, factorUnits.getLocalname());
    }

    public static Stream<Arguments> testGetLocalName() {
        return Stream.of(
                Arguments.of(FactorUnits.ofFactorUnitSpec(M2, 1, M, -1), "M2-PER-M"),
                Arguments.of(FactorUnits.ofFactorUnitSpec(M2, 1, M, -2), "M2-PER-M2"),
                Arguments.of(FactorUnits.ofFactorUnitSpec(RAD, 1, SEC, -1), "RAD-PER-SEC"),
                Arguments.of(FactorUnits.ofFactorUnitSpec(RAD, 1), "RAD"),
                Arguments.of(FactorUnits.ofFactorUnitSpec(BigDecimal.valueOf(1000), RAD, 1), "RAD"),
                Arguments.of(FactorUnits.ofFactorUnitSpec(N, 1, M, 1, M, -2), "N-M-PER-M2"),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(TON_Metric, 1, A, -3), "TON_Metric-PER-A3"));
    }

    @ParameterizedTest
    @MethodSource
    public void testGenerateAllLocalnamePossibilities(
            FactorUnits factorUnits, List<String> expectedResult) {
        assertEquals(expectedResult, factorUnits.generateAllLocalnamePossibilities());
    }

    public static Stream<Arguments> testGenerateAllLocalnamePossibilities() {
        return Stream.of(
                Arguments.of(FactorUnits.ofFactorUnitSpec(M2, 1, M, -1), List.of("M2-PER-M")),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(N, 1, M, 1, M, -2),
                        List.of("N-M-PER-M2", "M-N-PER-M2")),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(J, 1, KiloGM, -1, K, -1, PA, -1),
                        List.of(
                                "J-PER-KiloGM-K-PA",
                                "J-PER-KiloGM-PA-K",
                                "J-PER-K-KiloGM-PA",
                                "J-PER-K-PA-KiloGM",
                                "J-PER-PA-KiloGM-K",
                                "J-PER-PA-K-KiloGM")));
    }

    @ParameterizedTest
    @MethodSource
    public void testNumerator(FactorUnits factorUnits, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnits.numerator());
    }

    public static Stream<Arguments> testNumerator() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        FactorUnits.ofFactorUnitSpec(M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, -1),
                        FactorUnits.ofFactorUnitSpec()),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1),
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.KiloGM, 1, Units.SEC, -2),
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, KiloGM, 1)));
    }

    @ParameterizedTest
    @MethodSource
    public void testDenominator(FactorUnits factorUnits, FactorUnits expectedResult) {
        assertEquals(expectedResult, factorUnits.denominator());
    }

    public static Stream<Arguments> testDenominator() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        FactorUnits.ofFactorUnitSpec(M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, -1),
                        FactorUnits.ofFactorUnitSpec(Units.M, 2, Units.M, 1)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1),
                        FactorUnits.ofFactorUnitSpec()),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.KiloGM, 1, Units.SEC, -2),
                        FactorUnits.ofFactorUnitSpec(Units.SEC, 2)));
    }

    @ParameterizedTest
    @MethodSource
    public void testhasQkdvNumeratorIri(
            FactorUnits factorUnits, String dimensionVector, boolean expectedResult) {
        assertEquals(expectedResult, factorUnits.hasQkdvNumeratorIri(dimensionVector));
    }

    public static Stream<Arguments> testhasQkdvNumeratorIri() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        Units.M2.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        MilliM2.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        Units.M.getDimensionVectorIri().get(),
                        false),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, -1),
                        QuantityKinds.Dimensionless.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1),
                        Units.M2.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.KiloGM, 1, Units.SEC, -2),
                        M__KiloGM.getDimensionVectorIri().get(),
                        true));
    }

    @ParameterizedTest
    @MethodSource
    public void testhasQkdvDenominatorIri(
            FactorUnits factorUnits, String dimensionVector, boolean expectedResult) {
        assertEquals(expectedResult, factorUnits.hasQkdvDenominatorIri(dimensionVector));
    }

    public static Stream<Arguments> testhasQkdvDenominatorIri() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        Units.M2.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        MilliM2.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, 2),
                        Units.M.getDimensionVectorIri().get(),
                        false),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, -2, Units.M, -1),
                        Units.M3.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.M, 1),
                        QuantityKinds.Dimensionless.getDimensionVectorIri().get(),
                        true),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.KiloGM, 1, Units.SEC, -2),
                        SEC2.getDimensionVectorIri().get(),
                        true));
    }

    @ParameterizedTest
    @MethodSource
    public void testSortAccordingToUnitLabel(
            List<FactorUnit> factorUnits, String label, List<FactorUnit> expectedResult) {
        assertEquals(expectedResult, FactorUnits.sortAccordingToUnitLabel(label, factorUnits));
    }

    public static Stream<Arguments> testSortAccordingToUnitLabel() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 1, Units.N, 1).getFactorUnits(),
                        "N-M",
                        FactorUnits.ofFactorUnitSpec(Units.N, 1, Units.M, 1).getFactorUnits()),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.SEC, -2, Units.M, 1, Units.KiloGM, 1)
                                .getFactorUnits(),
                        "N",
                        FactorUnits.ofFactorUnitSpec(Units.SEC, -2, Units.M, 1, Units.KiloGM, 1)
                                .getFactorUnits()));
    }

    @Test
    public void getSymbol() {
        FactorUnits m8 = FactorUnits.ofFactorUnitSpec(M, 8);
        Optional<String> symbol = m8.getSymbol();
        Assertions.assertEquals("m⁸", symbol.get());

        FactorUnits ms12 = FactorUnits.ofFactorUnitSpec(MilliSEC, 12);
        Assertions.assertEquals("ms¹²", ms12.getSymbol().get());
    }
}
