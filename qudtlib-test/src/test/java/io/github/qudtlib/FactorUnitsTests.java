package io.github.qudtlib;

import static io.github.qudtlib.model.Units.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
                                FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1).getFactorUnits(),
                                BigDecimal.valueOf(1))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(GM, 2),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.KiloGM, 2).getFactorUnits(),
                                BigDecimal.valueOf(0.000001))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(GM, 2, GM, -2),
                        FactorUnits.ofFactorUnitSpec(KiloGM, 2, KiloGM, -2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(GM, 1, Units.M, -3),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E-3))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(GM, 1, Units.M3, -1),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E-3))));
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
    public void testGetConversionMultiplier(FactorUnits factorUnits, BigDecimal expectedResult) {
        assertThat(
                factorUnits.getConversionMultiplier(),
                is(closeTo(expectedResult, new BigDecimal("0.00001"))));
    }

    public static Stream<Arguments> testGetConversionMultiplier() {
        return Stream.of(
                        M,
                        M2,
                        BAR,
                        PER__BAR,
                        N,
                        N__M,
                        J,
                        J__PER__K,
                        BTU_IT__FT__PER__FT2__HR__DEG_F,
                        KiloCAL__PER__MOL,
                        ATM__M3__PER__MOL,
                        QT_UK__PER__DAY,
                        QT_UK__PER__HR,
                        CentiM6,
                        MilliGAL)
                .map(u -> Arguments.of(u.getFactorUnits(), u.getConversionMultiplier().get()));
    }

    @ParameterizedTest
    @MethodSource
    public void testConversionFactor(
            FactorUnits factorUnits, Unit toUnit, BigDecimal expectedResult) {
        assertThat(factorUnits.conversionFactor(toUnit), Matchers.comparesEqualTo(expectedResult));
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
                        new BigDecimal("0.00000001157407407407407407407407407407407")),
                Arguments.of(FactorUnits.ofFactorUnitSpec(A, 1), Units.AT, new BigDecimal("1.0")),
                Arguments.of(FactorUnits.ofFactorUnitSpec(AT, 1), Units.A, new BigDecimal("1.0")),
                Arguments.of(MilliGAL.getFactorUnits(), M__PER__SEC2, new BigDecimal("0.00001")));
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
                        FactorUnits.ofFactorUnitSpec(Units.SEC, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.KiloN, 1, Units.MilliM, 1),
                        FactorUnits.ofFactorUnitSpec()));
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
        assertEquals(expectedResult, FactorUnits.sortAccordingToUnitLocalname(label, factorUnits));
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

    @ParameterizedTest
    @MethodSource
    public void getSymbol(String expectedSymbol, FactorUnits factorUnits) {
        Assertions.assertEquals(
                expectedSymbol,
                factorUnits.getSymbol().orElse("[no symbol]"),
                String.format(
                        "Factor units %s should return %s for getSymbol()",
                        factorUnits, expectedSymbol));
    }

    public static Stream<Arguments> getSymbol() {
        return Stream.of(
                Arguments.of("m", FactorUnits.ofFactorUnitSpec(M, 1)),
                Arguments.of("/m", FactorUnits.ofFactorUnitSpec(M, -1)),
                Arguments.of("m⁸", FactorUnits.ofFactorUnitSpec(M, 8)),
                Arguments.of("ms¹²", FactorUnits.ofFactorUnitSpec(MilliSEC, 12)),
                Arguments.of("/ms⁹", FactorUnits.ofFactorUnitSpec(MilliSEC, -9)),
                Arguments.of("ft·h/gal{UK}", FT__HR__PER__GAL_UK.getFactorUnits()));
    }

    @ParameterizedTest
    @MethodSource
    public void testConversionMultiplierOpt(Unit unit) {
        Optional<BigDecimal> result = unit.getFactorUnits().getConversionMultiplierOpt();
        if (hasNoFactorUnitsWithoutConversionMultiplier(unit)) {
            assertTrue(
                    result.isPresent(),
                    String.format(
                            "%s.getConversionMultiplierOpt() is expected to return a value",
                            unit.getIriAbbreviated()));
            assertThat(
                    unit.getFactorUnits().getConversionMultiplierWithFallbackOne(),
                    is(closeTo(result.get(), new BigDecimal("0.00001"))));

        } else {
            assertTrue(
                    result.isEmpty(),
                    String.format(
                            "%s.getConversionMultiplierOpt() is expected to return Optional.empty()",
                            unit.getIriAbbreviated()));
        }
    }

    public static Stream<Arguments> testConversionMultiplierOpt() {
        return Qudt.allUnits().stream().map(u -> Arguments.of(u));
    }

    @Disabled
    @Test
    public void testIssue100() {
        // 3 km^3 * 5 hr / 87654 N^2. I would like to reduce this to 6.16e+8 m s5 / kg2.

        // that's one way to get the factor units:
        FactorUnits factorUnits = FactorUnits.ofFactorUnitSpec(KiloM, 3, HR, 1, N, -2);
        // Note: you could also get there this way
        // factorUnits =
        // KiloM.getFactorUnits().pow(3).combineWith(HR.getFactorUnits()).combineWith(N.getFactorUnits().pow(-2));

        System.out.println("factorUnits: " + factorUnits);
        System.out.println("symbol: " + factorUnits.getSymbol());
        System.out.println("scaleFactor: " + factorUnits.getScaleFactor());
        System.out.println("-------------");

        FactorUnits factorUnitsNormalized = factorUnits.normalize();
        System.out.println("factorUnits normalized: " + factorUnitsNormalized);
        System.out.println("symbol: " + factorUnitsNormalized.getSymbol());
        System.out.println("scaleFactor: " + factorUnitsNormalized.getScaleFactor());
        System.out.println("-------------");

        Unit newUnit =
                Unit.definition(QudtNamespaces.unit.getBaseIri(), factorUnitsNormalized).build();
        System.out.println("synthetic unit localname: " + newUnit.getIriLocalname());
        System.out.println("symbol: " + newUnit.getSymbol());
        System.out.println("-------------");
        Unit newUnitWithKiloGM =
                Unit.definition(
                                QudtNamespaces.unit.getBaseIri(),
                                FactorUnits.ofFactorUnitSpec(M, 1, SEC, 5, KiloGM, -2))
                        .build();
        System.out.println(
                "better synthetic unit localname: " + newUnitWithKiloGM.getIriLocalname());
        System.out.println("symbol: " + newUnitWithKiloGM.getSymbol());
        System.out.println("-------------");
        MathContext mc = MathContext.DECIMAL128;
        QuantityValue q =
                new QuantityValue(
                        new BigDecimal("3")
                                .multiply(new BigDecimal("5"), mc)
                                .divide(new BigDecimal("87654"), mc),
                        newUnit);
        System.out.println("value: " + q);
        QuantityValue converted = q.convert(newUnitWithKiloGM);
        System.out.println("converted value: " + converted);
        System.out.println("-------------");
        /**
         * possible future development: QuantityValue q1 = new QuantityValue(new BigDecimal(3),
         * Unit.definition(QudtNamespaces.unit.getBaseIri(), FactorUnits.ofFactorUnitSpec(KiloM,
         * 3)).build()); QuantityValue q2 = new QuantityValue(new BigDecimal(5), HR); QuantityValue
         * q3 = new QuantityValue(new BigDecimal(87654),
         * Unit.definition(QudtNamespaces.unit.getBaseIri(), FactorUnits.ofFactorUnitSpec(N,
         * 2)).build()); QuantityValue result = q1.multiply(q2).divide(q3); QuantityValue scaled =
         * result.convert(Qudt.scaleFactorUnit(result.getUnit(), GM, KiloGM));
         */
        q = new QuantityValue(new BigDecimal(1), newUnit);
        System.out.println("value: " + q);
        System.out.println("converted " + q.convert(newUnitWithKiloGM));

        q = new QuantityValue(new BigDecimal(1), PER__GM);
        System.out.println("value: " + q);
        System.out.println("converted " + q.convert(PER__KiloGM));

        q = new QuantityValue(new BigDecimal(1), KiloGM__PER__SEC);
        System.out.println("value: " + q);
        System.out.println("converted " + q.convert(GM__PER__SEC));

        q = new QuantityValue(new BigDecimal(1), GM);
        System.out.println("value: " + q);
        System.out.println("converted " + q.convert(KiloGM));
    }

    @Disabled
    @Test
    public void testIssue100_solved() {
        // 3 km^3 * 5 hr / 87654 N^2. I would like to reduce this to 6.16e+8 m s5 / kg2.

        // that's one way to get the factor units:
        FactorUnits factorUnits = FactorUnits.ofFactorUnitSpec(KiloM, 3, HR, 1, N, -2);
        // Note: you could also get there this way
        // factorUnits =
        // KiloM.getFactorUnits().pow(3).combineWith(HR.getFactorUnits()).combineWith(N.getFactorUnits().pow(-2));

        System.out.println("factorUnits: " + factorUnits);
        System.out.println("symbol: " + factorUnits.getSymbol());
        System.out.println("scaleFactor: " + factorUnits.getScaleFactor());
        System.out.println("-------------");

        FactorUnits factorUnitsNormalized = factorUnits.normalize();
        System.out.println("factorUnits normalized: " + factorUnitsNormalized);
        System.out.println("symbol: " + factorUnitsNormalized.getSymbol());
        System.out.println("scaleFactor: " + factorUnitsNormalized.getScaleFactor());
        System.out.println("-------------");

        Unit newUnit =
                Unit.definition(
                                QudtNamespaces.unit.getBaseIri(),
                                factorUnitsNormalized.withoutScaleFactor())
                        //                                   ^^^^^^^^^^^^^^^^^^^^^---note this
                        .build();
        System.out.println("synthetic unit localname: " + newUnit.getIriLocalname());
        System.out.println("symbol: " + newUnit.getSymbol());
        System.out.println("scaleFactor: " + newUnit.getFactorUnits().getScaleFactor());
        System.out.println("-------------");
        Unit newUnitWithKiloGM =
                Unit.definition(
                                QudtNamespaces.unit.getBaseIri(),
                                FactorUnits.ofFactorUnitSpec(M, 1, SEC, 5, KiloGM, -2))
                        .build();
        System.out.println(
                "better synthetic unit localname: " + newUnitWithKiloGM.getIriLocalname());
        System.out.println("symbol: " + newUnitWithKiloGM.getSymbol());
        System.out.println("scaleFactor: " + newUnitWithKiloGM.getFactorUnits().getScaleFactor());
        System.out.println("-------------");
        MathContext mc = MathContext.DECIMAL128;
        BigDecimal value =
                new BigDecimal("3")
                        .multiply(new BigDecimal("5"), mc)
                        .divide(new BigDecimal("87654"), mc);
        QuantityValue q =
                new QuantityValue(
                        value.multiply(factorUnitsNormalized.getScaleFactor()),
                        //                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^---note
                        // this
                        newUnit);
        System.out.println("value: " + q);
        QuantityValue converted = q.convert(newUnitWithKiloGM);
        System.out.println("converted value: " + converted);
        System.out.println("-------------");
        /**
         * possible future development: QuantityValue q1 = new QuantityValue(new BigDecimal(3),
         * Unit.definition(QudtNamespaces.unit.getBaseIri(), FactorUnits.ofFactorUnitSpec(KiloM,
         * 3)).build()); QuantityValue q2 = new QuantityValue(new BigDecimal(5), HR); QuantityValue
         * q3 = new QuantityValue(new BigDecimal(87654),
         * Unit.definition(QudtNamespaces.unit.getBaseIri(), FactorUnits.ofFactorUnitSpec(N,
         * 2)).build()); QuantityValue result = q1.multiply(q2).divide(q3); QuantityValue scaled =
         * result.convert(Qudt.scaleFactorUnit(result.getUnit(), GM, KiloGM));
         */
    }

    private boolean hasNoFactorUnitsWithoutConversionMultiplier(Unit unit) {
        FactorUnits reduced =
                unit.getFactorUnits().reduceExponents(); // reduce here because we also reduce in
        if (reduced.getFactorUnits().size() == 0) {
            return true; // all units cancel out, we have no missing units
        }
        // FactorUnits.getConversionMultiplierOpt()
        if (reduced.hasFactorUnits()) {
            return reduced.getFactorUnits().stream()
                    .allMatch(fu -> hasNoFactorUnitsWithoutConversionMultiplier(fu.getUnit()));
        }
        return unit.getConversionMultiplier().isPresent();
    }
}
