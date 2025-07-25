package io.github.qudtlib;

import static io.github.qudtlib.model.QuantityKinds.TemperatureDifference;
import static io.github.qudtlib.model.Units.*;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.algorithm.AssignmentProblem;
import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.model.*;
import io.github.qudtlib.model.Unit.Definition;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.Preconditions;

/**
 * Unit tests for QUDTLib functionality.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class QudtTests {
    @Test
    public void testPrefix() {
        Prefix kilo = Qudt.Prefixes.Kilo;
        Assertions.assertNotNull(kilo);
        MatcherAssert.assertThat(
                new BigDecimal("1000"), Matchers.comparesEqualTo(kilo.getMultiplier()));
        Assertions.assertEquals(Qudt.prefixRequired(kilo.getIri()), kilo);
        Assertions.assertEquals(Qudt.prefixRequired(kilo.getIri()), kilo);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testUnit() {
        Unit metre = Qudt.Units.M;
        Assertions.assertTrue(metre.hasLabel("Metre"));
        Assertions.assertTrue(metre.hasLabel("Meter"));
        Assertions.assertEquals("Metre", metre.getLabelForLanguageTag("en").get().getString());
        Assertions.assertEquals(Qudt.unitRequired(metre.getIri()), metre);
        Assertions.assertEquals(Qudt.unitRequired(metre.getIri()), metre);
    }

    @Test
    public void testQuantityKind() {
        QuantityKind length = Qudt.QuantityKinds.Length;
        Assertions.assertTrue(length.hasLabel("length"));
        Assertions.assertEquals(Qudt.quantityKindRequired(length.getIri()), length);
        Assertions.assertEquals(Qudt.quantityKindRequired(length.getIri()), length);
    }

    @Test
    public void testQuantityKindForUnit() {
        Unit unit = Qudt.unitFromLabelRequired("Newton Meter");
        Set<QuantityKind> broad = Qudt.quantityKinds(unit);
        Assertions.assertTrue(broad.contains(Qudt.quantityKindFromLocalnameRequired("Torque")));
        Assertions.assertTrue(
                broad.contains(Qudt.quantityKindFromLocalnameRequired("MomentOfForce")));
        unit = Qudt.Units.PA__PER__BAR;
        broad = Qudt.quantityKindsBroad(unit);
        Assertions.assertTrue(broad.contains(Qudt.QuantityKinds.PressureRatio));
        Assertions.assertTrue(broad.contains(Qudt.QuantityKinds.DimensionlessRatio));
    }

    @Test
    public void testDerivedUnitFromMap() {
        Assertions.assertTrue(
                Qudt.unitsFromMap(DerivedUnitSearchMode.BEST_MATCH, Map.of(Qudt.Units.M, -3))
                        .contains(Qudt.Units.PER__M3));
        Assertions.assertTrue(
                Qudt.unitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH,
                                Qudt.Units.MilliA,
                                1,
                                Qudt.Units.IN,
                                -1)
                        .contains(Qudt.Units.MilliA__PER__IN));
        Assertions.assertTrue(
                Qudt.unitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH,
                                Qudt.Units.MOL,
                                1,
                                Qudt.Units.M,
                                -2,
                                Qudt.Units.SEC,
                                -1)
                        .contains(Qudt.Units.MOL__PER__M2__SEC));
    }

    @Test
    public void testUnitFromLabel() {
        Assertions.assertEquals(Qudt.Units.N, Qudt.unitFromLabelRequired("Newton"));
        Assertions.assertEquals(Qudt.Units.M, Qudt.unitFromLabelRequired("Metre"));
        Assertions.assertEquals(Qudt.Units.M2, Qudt.unitFromLabelRequired("SQUARE_METRE"));
        Assertions.assertEquals(Qudt.Units.M2, Qudt.unitFromLabelRequired("SQUARE METRE"));
        Assertions.assertEquals(Qudt.Units.M3, Qudt.unitFromLabelRequired("Cubic Metre"));
        Assertions.assertEquals(Qudt.Units.GM, Qudt.unitFromLabelRequired("Gram"));
        Assertions.assertEquals(Qudt.Units.SEC, Qudt.unitFromLabelRequired("second"));
        Assertions.assertEquals(Qudt.Units.HZ, Qudt.unitFromLabelRequired("Hertz"));
        Assertions.assertEquals(Qudt.Units.DEG_C, Qudt.unitFromLabelRequired("degree celsius"));
        Assertions.assertEquals(Qudt.Units.DEG_F, Qudt.unitFromLabelRequired("degree fahrenheit"));
        Assertions.assertEquals(Qudt.Units.A, Qudt.unitFromLabelRequired("ampere"));
        Assertions.assertEquals(Qudt.Units.V, Qudt.unitFromLabelRequired("volt"));
        Assertions.assertEquals(Qudt.Units.W, Qudt.unitFromLabelRequired("Watt"));
        Assertions.assertEquals(Qudt.Units.LUX, Qudt.unitFromLabelRequired("Lux"));
        Assertions.assertEquals(Qudt.Units.LM, Qudt.unitFromLabelRequired("Lumen"));
        Assertions.assertEquals(Qudt.Units.CD, Qudt.unitFromLabelRequired("Candela"));
        Assertions.assertEquals(PA, Qudt.unitFromLabelRequired("Pascal"));
        Assertions.assertEquals(Qudt.Units.RAD, Qudt.unitFromLabelRequired("Radian"));
        Assertions.assertEquals(Qudt.Units.J, Qudt.unitFromLabelRequired("Joule"));
        Assertions.assertEquals(Qudt.Units.K, Qudt.unitFromLabelRequired("Kelvin"));
        Assertions.assertEquals(Qudt.Units.SR, Qudt.unitFromLabelRequired("Steradian"));
    }

    @Test
    public void testUnitFromFactors() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        Qudt.unitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M));
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.GM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.GM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.MOL,
                        1,
                        Qudt.Units.M,
                        -2,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.MOL__PER__M2__SEC));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.K,
                        1,
                        Qudt.Units.M,
                        2,
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.K__M2__PER__KiloGM__SEC));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.BTU_IT,
                        1,
                        Qudt.Units.FT,
                        1,
                        Qudt.Units.FT,
                        -2,
                        Qudt.Units.HR,
                        -1,
                        Qudt.Units.DEG_F,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.BTU_IT__FT__PER__FT2__HR__DEG_F));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 1);
        Assertions.assertTrue(units.contains(Qudt.Units.M));
        Assertions.assertFalse(units.contains(Qudt.Units.RAD)); // m per m should not match here!
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.A, -1);
        Assertions.assertEquals(0, units.size());
    }

    @Test
    public void testDerivedUnit() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.K, -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, -2);
        Assertions.assertEquals(Qudt.Units.PER__M2, units.stream().findFirst().get());
    }

    @Test
    public void testDerivedUnitByIri() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.K.getIri(), -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), -2);
        Assertions.assertEquals(Qudt.Units.PER__M2, units.stream().findFirst().get());
    }

    @Test
    public void testDerivedUnitByLocalname() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "K", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", -2);
        Assertions.assertEquals(Qudt.Units.PER__M2, units.stream().findFirst().get());
    }

    @Test
    public void testDerivedUnitByLabel() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "Meter", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "Metre", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "Kelvin", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "Bar", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__BAR));
    }

    @Test
    public void testDerivedUnit2() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 1, Qudt.Units.N, 1);
        Assertions.assertEquals(J, units.stream().findFirst().get());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.scale("Kilo", "Gram"),
                        1,
                        Qudt.Units.M,
                        -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.N, 1, Qudt.Units.M, -2);
        Assertions.assertEquals(Qudt.Units.PA, units.stream().findFirst().get());
        Assertions.assertEquals(1, units.size());
    }

    @Test
    public void testDerivedUnit3() {
        // test making sure overspecifying factors are rejected
        Assertions.assertTrue(
                Qudt.unitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH,
                                Qudt.Units.M,
                                1,
                                Qudt.Units.N,
                                1,
                                Qudt.Units.SEC,
                                -2)
                        .isEmpty());
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.MOL,
                        1,
                        Qudt.Units.M,
                        -2,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.MOL__PER__M2__SEC));
    }

    @Test
    public void testDerivedUnit4() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.K,
                        1,
                        Qudt.Units.M,
                        2,
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.K__M2__PER__KiloGM__SEC));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        -2);
        Assertions.assertEquals(PA, units.stream().findFirst().get());
    }

    @Test
    public void testDerivedUnit5() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.BTU_IT,
                        1,
                        Qudt.Units.FT,
                        1,
                        Qudt.Units.FT,
                        -2,
                        Qudt.Units.HR,
                        -1,
                        Qudt.Units.DEG_F,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.BTU_IT__FT__PER__FT2__HR__DEG_F));
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        -2,
                        Qudt.Units.M,
                        1);
        Assertions.assertEquals(Qudt.Units.N__PER__M, units.stream().findFirst().get());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.M,
                        2,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        -2);
        Assertions.assertEquals(Qudt.Units.N__PER__M, units.stream().findFirst().get());
    }

    @Test
    public void testScaledUnit() {
        Unit unit = Qudt.scale("Nano", "Meter");
        Assertions.assertEquals(Qudt.Units.NanoM, unit);
        unit = Qudt.scale("Giga", "Hertz");
        Assertions.assertEquals(Qudt.Units.GigaHZ, unit);
        unit = Qudt.scale("Kilo", "Gram");
        Assertions.assertEquals(Qudt.Units.KiloGM, unit);
        unit = Qudt.scale("KILO", "GRAM");
        Assertions.assertEquals(Qudt.Units.KiloGM, unit);
        unit = Qudt.scale(Qudt.Prefixes.Nano, Qudt.Units.M);
        Assertions.assertEquals(Qudt.Units.NanoM, unit);
        unit = Qudt.scale(Qudt.Prefixes.Kilo, Units.GM);
        Assertions.assertEquals(Qudt.Units.KiloGM, unit);
    }

    @Test
    public void testGetUnitFactors() {
        Unit unit = Qudt.unitFromLabelRequired("newton meter");
        List<FactorUnit> unitFactors = Qudt.factorUnits(unit);
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("meter"), 2)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("kilogram"), 1)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("second"), -2)));
        unit = Qudt.unitFromLabelRequired("newton meter per square meter");
        unitFactors = Qudt.factorUnits(unit);
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("meter"), 2)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("meter"), -2)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("kilogram"), 1)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("second"), -2)));
        unit = Qudt.Units.KiloN__M;
        unitFactors = Qudt.factorUnits(unit);
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("meter"), 2)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("kilogram"), 1)));
        Assertions.assertTrue(
                unitFactors.contains(new FactorUnit(Qudt.unitFromLabelRequired("second"), -2)));
    }

    @Test
    public void testGetUnitFactorsUnscaled() {
        List<FactorUnit> unitFactors =
                Qudt.unscale(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M, 2, SEC, -2).getFactorUnits(),
                        false,
                        false);
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.KiloGM, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 2)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.SEC, -2)));
        unitFactors =
                Qudt.unscale(
                        FactorUnits.ofFactorUnitSpec(GM, 1, M, 2, SEC, -2).getFactorUnits(),
                        false,
                        false);
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.KiloGM, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 2)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.SEC, -2)));
    }

    @ParameterizedTest
    @MethodSource
    public void testUnscale_Unit(Unit unit, Unit expectedResult) {
        assertEquals(expectedResult, Qudt.unscale(unit));
    }

    public static Stream<Arguments> testUnscale_Unit() {
        return Stream.of(
                Arguments.of(Units.YoctoC, Units.C),
                Arguments.of(Units.TeraBYTE, Units.BYTE),
                Arguments.of(Units.KiloGM, Units.KiloGM),
                Arguments.of(Units.MilliGM, Units.GM),
                Arguments.of(Units.MegaGM, Units.GM),
                Arguments.of(Units.TON_Metric, TON_Metric),
                Arguments.of(Units.TONNE, TONNE),
                Arguments.of(Units.KiloM, Units.M),
                Arguments.of(Units.KiloN, Units.N));
    }

    @Test
    public void testUnitless() {
        Assertions.assertEquals(
                new BigDecimal("1.1234"),
                Qudt.convert(
                        new BigDecimal("1.1234"), Qudt.Units.KiloGM__PER__M3, Qudt.Units.UNITLESS));
        Assertions.assertEquals(
                new BigDecimal("1.1234"),
                Qudt.convert(
                        new BigDecimal("1.1234"), Qudt.Units.UNITLESS, Qudt.Units.KiloGM__PER__M3));
        Assertions.assertEquals(
                new BigDecimal("1.1234"),
                Qudt.convert(new BigDecimal("1.1234"), Qudt.Units.UNITLESS, Qudt.Units.UNITLESS));
    }

    @Test
    public void testConvert_N_to_kN() {
        BigDecimal converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.N, Qudt.Units.KiloN);
        MatcherAssert.assertThat(converted, Matchers.comparesEqualTo(new BigDecimal("0.001")));
    }

    @Test
    public void testInconvertible() {
        assertThrows(
                InconvertibleQuantitiesException.class,
                () -> Qudt.convert(BigDecimal.ONE, Qudt.Units.SEC, Qudt.Units.M));
    }

    @Test
    public void testConvert_L_to_GAL_US() {
        BigDecimal converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.L, Qudt.Units.GAL_US);
        MatcherAssert.assertThat(
                converted,
                Matchers.comparesEqualTo(new BigDecimal("0.2641720523581484153798999216091625")));
    }

    @Test
    public void testConvert_Celsius_to_Fahrenheit() {
        QuantityValue celsius100 =
                new QuantityValue(new BigDecimal("100"), Qudt.unitFromLocalnameRequired("DEG_C"));
        QuantityValue fahrenheit = Qudt.convert(celsius100, Qudt.unitIriFromLocalname("DEG_F"));
        Assertions.assertNotNull(fahrenheit);
        MatcherAssert.assertThat(
                fahrenheit.getValue(),
                Matchers.comparesEqualTo(new BigDecimal("211.9999999999999999999999999999999")));
        Assertions.assertEquals(Qudt.unitIriFromLocalname("DEG_F"), fahrenheit.getUnit().getIri());
    }

    @Test
    public void testConvert_Celsius_to_Fahrenheit_tempdiff() {
        QuantityValue celsius100 =
                new QuantityValue(new BigDecimal("100"), Qudt.unitFromLocalnameRequired("DEG_C"));
        QuantityValue fahrenheit = Qudt.convert(celsius100, DEG_F, TemperatureDifference);
        Assertions.assertNotNull(fahrenheit);
        MatcherAssert.assertThat(
                fahrenheit.getValue(), Matchers.comparesEqualTo(new BigDecimal("180.0")));
        Assertions.assertEquals(Qudt.unitIriFromLocalname("DEG_F"), fahrenheit.getUnit().getIri());
    }

    @Test
    public void testConvert_Celsius_to_Kelvin_tempdiff() {
        QuantityValue celsius100 =
                new QuantityValue(new BigDecimal("100"), Qudt.unitFromLocalnameRequired("DEG_C"));
        QuantityValue kelvin = Qudt.convert(celsius100, K, TemperatureDifference);
        Assertions.assertNotNull(kelvin);
        MatcherAssert.assertThat(
                kelvin.getValue(), Matchers.comparesEqualTo(new BigDecimal("100")));
        Assertions.assertEquals(Qudt.unitIriFromLocalname("K"), kelvin.getUnit().getIri());
    }

    @Test
    public void testConvert_Celsius_to_Fahrenheit_2() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("100"), Units.DEG_C, Units.DEG_F),
                Matchers.comparesEqualTo(new BigDecimal("211.9999999999999999999999999999999")));
    }

    @Test
    public void testConvert_Fahrenheit_to_Celsius() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("100"), Units.DEG_F, Units.DEG_C),
                Matchers.comparesEqualTo(new BigDecimal("37.7777777777777777777777777777778")));
    }

    @Test
    public void testConvert_byte_to_megabyte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1000000"), Units.BYTE, Units.MegaBYTE),
                Matchers.comparesEqualTo(BigDecimal.ONE));
    }

    @Test
    public void testConvert_megabyte_to_byte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1"), Units.MegaBYTE, Units.BYTE),
                Matchers.comparesEqualTo(new BigDecimal("1000000")));
    }

    @Test
    public void testMilliSV__PER__HR_to_MicroSV__PER__HR() {
        /*
        Stream.of(Units.HR, PER__HR, SV, MilliSV, MicroSV, MilliSV__PER__HR, MicroSV__PER__HR)
                .forEach(
                        u ->
                                System.out.println(
                                        String.format(
                                                "%30s: %s",
                                                u.getIriLocalname(),
                                                u.getConversionMultiplier().get().toString())));
         */
        BigDecimal converted =
                Qudt.convert(
                        new BigDecimal("15.12"), Units.MilliSV__PER__HR, Units.MicroSV__PER__HR);
        MatcherAssert.assertThat(converted, Matchers.comparesEqualTo(new BigDecimal(15120)));
        BigDecimal expected = new BigDecimal(15120);
        assertEquals(expected, converted);
    }

    @Test
    public void testGetConversionMultiplier() {
        MatcherAssert.assertThat(
                Units.CentiM.getConversionMultiplier(Units.MilliM),
                Matchers.comparesEqualTo(new BigDecimal("10")));
        MatcherAssert.assertThat(
                Units.MilliM.getConversionMultiplier(Units.KiloM),
                Matchers.comparesEqualTo(new BigDecimal("0.000001")));
        assertThrows(
                IllegalArgumentException.class,
                () -> Qudt.Units.DEG_F.getConversionMultiplier(Qudt.Units.DEG_C));
        MatcherAssert.assertThat(
                Units.MilliGAL.getConversionMultiplier(Units.M__PER__SEC2),
                Matchers.comparesEqualTo(new BigDecimal("0.00001")));
    }

    @Test
    public void testConvert_FemtoGM_to_KiloGM() {
        BigDecimal converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.FemtoGM, Qudt.Units.KiloGM);
        MatcherAssert.assertThat(
                converted, Matchers.comparesEqualTo(new BigDecimal("0.000000000000000001")));
    }

    @Test
    public void testConvert_Metric_to_Imperial() {
        BigDecimal converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.LB, Qudt.Units.KiloGM);
        MatcherAssert.assertThat(converted, Matchers.comparesEqualTo(new BigDecimal("0.45359237")));
        converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.BTU_IT__PER__LB, Qudt.Units.J__PER__GM);
        MatcherAssert.assertThat(converted, Matchers.comparesEqualTo(new BigDecimal("2.326")));
    }

    @Test
    public void testContractExponents() {
        List<FactorUnit> result =
                FactorUnit.contractExponents(
                        List.of(new FactorUnit(Qudt.Units.M, 2), new FactorUnit(Qudt.Units.M, -1)));
        assertEquals(2, result.size());
        assertTrue(result.contains(new FactorUnit(Qudt.Units.M, -1)));
        assertTrue(result.contains(new FactorUnit(Qudt.Units.M, 2)));
        result =
                FactorUnit.contractExponents(
                        List.of(new FactorUnit(Qudt.Units.M, 2), new FactorUnit(Qudt.Units.M, 1)));
        assertEquals(1, result.size());
        assertEquals(new FactorUnit(Qudt.Units.M, 3), result.get(0));
        result =
                FactorUnit.contractExponents(
                        List.of(
                                new FactorUnit(Qudt.Units.M, 2),
                                new FactorUnit(Qudt.Units.M, -2),
                                new FactorUnit(Qudt.Units.SEC, -3),
                                new FactorUnit(Qudt.Units.SEC, -1)));
        assertEquals(3, result.size());
        assertTrue(result.contains(new FactorUnit(Qudt.Units.M, 2)));
        assertTrue(result.contains(new FactorUnit(Qudt.Units.M, -2)));
        assertTrue(result.contains(new FactorUnit(Qudt.Units.SEC, -4)));
    }

    @ParameterizedTest
    @MethodSource
    public void assignmentProblemSolverTest(double[][] mat, int[] assignment) {
        AssignmentProblem.Instance instance = AssignmentProblem.instance(mat);
        AssignmentProblem.Solution solution = instance.solve();
        Assertions.assertArrayEquals(assignment, solution.getAssignment());
    }

    public static Stream<Arguments> assignmentProblemSolverTest() {
        return Stream.of(
                Arguments.of(
                        new double[][] {
                            {0, 1},
                            {1, 0},
                        },
                        new int[] {0, 1}),
                Arguments.of(
                        new double[][] {
                            {1, 0},
                            {0, 1},
                        },
                        new int[] {1, 0}),
                Arguments.of(
                        new double[][] {
                            {0, 0},
                            {0, 0},
                        },
                        new int[] {0, 1}),
                Arguments.of(
                        new double[][] {
                            {0, 1, 0},
                            {2, 5, 3},
                        },
                        new int[] {2, 0}),
                Arguments.of(
                        new double[][] {
                            {0, 1, 0, 2},
                            {1, 0, 2, 3},
                            {2, 4, 0, 0}
                        },
                        new int[] {0, 1, 2}),
                Arguments.of(
                        new double[][] {
                            {0, 1, 0, 2},
                            {1, 4, 3, 3},
                            {4, 5, 4, 6}
                        },
                        new int[] {2, 0, 1}));
    }

    @Test
    void assignmentProblemWrongShape() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        AssignmentProblem.instance(
                                new double[][] {
                                    {0, 1, 2},
                                    {2, 3, 4},
                                    {4, 5, 6},
                                    {7, 8, 9}
                                }));
    }

    @Test
    public void scoreOverlapMatrixTestLargestAssumableMatrix() {
        Random rnd = new Random(System.currentTimeMillis());
        double[][] mat = new double[10][10];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                mat[i][j] = rnd.nextDouble();
            }
        }
        long start = System.currentTimeMillis();
        AssignmentProblem.Instance instance = AssignmentProblem.instance(mat);
        AssignmentProblem.Solution solution = instance.solve();
        double score = solution.getWeight();
    }

    @ParameterizedTest
    @MethodSource("testAllPossibleUnitFactors")
    public void testAllPossibleUnitFactorsSize(int id, Unit unit, Object[][] expectedResult) {
        List<List<FactorUnit>> expectedResultConverted =
                Stream.of(expectedResult)
                        .map(objarr -> FactorUnits.ofFactorUnitSpec(objarr).getFactorUnits())
                        .collect(Collectors.toList());
        List<List<FactorUnit>> actualResult = unit.getAllPossibleFactorUnitCombinations();
        Assertions.assertEquals(expectedResultConverted.size(), actualResult.size());
    }

    @ParameterizedTest
    @MethodSource("testAllPossibleUnitFactors")
    public void testAllPossibleUnitFactorsActualResultIsExpected(
            int id, Unit unit, Object[][] expectedResult) {
        List<List<FactorUnit>> expectedResultConverted =
                Stream.of(expectedResult)
                        .map(objarr -> FactorUnits.ofFactorUnitSpec(objarr).getFactorUnits())
                        .collect(Collectors.toList());
        List<List<FactorUnit>> actualResult = unit.getAllPossibleFactorUnitCombinations();
        actualResult.stream()
                .forEach(
                        act ->
                                Assertions.assertTrue(
                                        expectedResultConverted.stream()
                                                .anyMatch(
                                                        exp ->
                                                                exp.stream()
                                                                        .allMatch(
                                                                                e ->
                                                                                        act.stream()
                                                                                                .anyMatch(
                                                                                                        a ->
                                                                                                                a
                                                                                                                        .equals(
                                                                                                                                e)))),
                                        () ->
                                                String.format(
                                                        "Actual result %s not in expected result %s",
                                                        act, expectedResult)));
    }

    @ParameterizedTest
    @MethodSource("testAllPossibleUnitFactors")
    public void testAllPossibleUnitFactorsExpectedResultIsPresent(
            int id, Unit unit, Object[][] expectedResult) {
        List<List<FactorUnit>> expectedResultConverted =
                Stream.of(expectedResult)
                        .map(objarr -> FactorUnits.ofFactorUnitSpec(objarr).getFactorUnits())
                        .collect(Collectors.toList());
        List<List<FactorUnit>> actualResult = unit.getAllPossibleFactorUnitCombinations();
        expectedResultConverted.stream()
                .forEach(
                        exp ->
                                Assertions.assertTrue(
                                        actualResult.stream()
                                                .anyMatch(
                                                        act ->
                                                                exp.stream()
                                                                        .allMatch(
                                                                                e ->
                                                                                        act.stream()
                                                                                                .anyMatch(
                                                                                                        a ->
                                                                                                                a
                                                                                                                        .equals(
                                                                                                                                e)))),
                                        () ->
                                                String.format(
                                                        "Expected result %s not found in actual result %s",
                                                        exp, actualResult)));
    }

    public static Stream<Arguments> testAllPossibleUnitFactors() {
        return Stream.of(
                Arguments.of(
                        2,
                        Units.N__M,
                        new Object[][] {
                            {Units.N__M, 1},
                            {Units.KiloGM, 1, Units.M, 2, Units.SEC, -2},
                            {Units.N, 1, Units.M, 1}
                        }),
                Arguments.of(
                        1,
                        Qudt.Units.N,
                        new Object[][] {
                            {Units.N, 1},
                            {Units.KiloGM, 1, Units.M, 1, Units.SEC, -2},
                        }),
                Arguments.of(
                        3,
                        Qudt.Units.N__M__PER__M2,
                        new Object[][] {
                            {Qudt.Units.N__M__PER__M2, 1},
                            {
                                Qudt.Units.N, 1,
                                Qudt.Units.M, -1
                            },
                            {
                                Qudt.Units.KiloGM, 1,
                                Qudt.Units.SEC, -2
                            },
                            {
                                Qudt.Units.N, 1,
                                Qudt.Units.M, 1,
                                Qudt.Units.M, -2
                            },
                            {
                                Qudt.Units.M, -2,
                                Qudt.Units.M, 2,
                                Qudt.Units.KiloGM, 1,
                                Qudt.Units.SEC, -2
                            },
                        }));
    }

    @MethodSource
    @ParameterizedTest
    public void testExactMatches(@AggregateWith(VarargsAggregator.class) Unit... units) {
        for (int i = 0; i < units.length; i++) {
            for (int j = i + 1; j < units.length; j++) {
                Unit u1 = units[i];
                Unit u2 = units[j];
                if (i != j) {
                    assertTrue(
                            u1.getExactMatches().contains(u2),
                            String.format(
                                    "(%s).getExactMatches().contains(%s)",
                                    Qudt.NAMESPACES.unit.abbreviate(u1.getIri()),
                                    Qudt.NAMESPACES.unit.abbreviate(u2.getIri())));
                    assertTrue(
                            u2.getExactMatches().contains(u1),
                            String.format(
                                    "(%s).getExactMatches().contains(%s)",
                                    Qudt.NAMESPACES.unit.abbreviate(u2.getIri()),
                                    Qudt.NAMESPACES.unit.abbreviate(u1.getIri())));
                }
            }
        }
    }

    public static Stream<Arguments> testExactMatches() {
        return Stream.of(
                Arguments.of(S__PER__M, KiloGM__PER__M2__PA__SEC),
                Arguments.of(KiloGM__PER__M2__SEC, KiloGM__PER__SEC__M2),
                Arguments.of(KiloTONNE, KiloTON_Metric),
                Arguments.of(LB_F__PER__IN2, PSI),
                Arguments.of(MHO, S),
                Arguments.of(MHO_Stat, S_Stat),
                Arguments.of(MIN_Angle, ARCMIN),
                Arguments.of(MI_N__PER__HR, KN),
                Arguments.of(MegaGM__PER__HA, TONNE__PER__HA, TON_Metric__PER__HA),
                Arguments.of(HectoPA, MilliBAR));
    }

    static class VarargsAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            Class<?> parameterType = context.getParameter().getType();
            Preconditions.condition(
                    parameterType.isArray(),
                    () -> "must be an array type, but was " + parameterType);
            Class<?> componentType = parameterType.getComponentType();
            return IntStream.range(context.getIndex(), accessor.size())
                    .mapToObj(index -> accessor.get(index, componentType))
                    .toArray(size -> (Object[]) Array.newInstance(componentType, size));
        }
    }

    @Test
    public void testSymbol() {
        Unit u = Qudt.Units.TONNE__PER__M3;
        Assertions.assertEquals("t/m³", u.getSymbol().get());
        Assertions.assertEquals("J/(kg·K)", J__PER__KiloGM__K.getSymbol().get());
    }

    @Test
    public void testUcumCode() {
        Assertions.assertEquals("t.m-3", Qudt.Units.TONNE__PER__M3.getUcumCode().get());
        Assertions.assertEquals("J.kg-1.K-1", J__PER__KiloGM__K.getUcumCode().get());
    }

    @Test
    public void nullOffsetDifference() {
        Optional<Unit> unit = Qudt.correspondingUnitInSystem(IN, Qudt.SystemsOfUnits.SI);
        Assertions.assertTrue(unit.isPresent());
        Assertions.assertEquals(Qudt.Units.CentiM, unit.get());
    }

    @Test
    public void unitFromFactorUnits() {
        FactorUnits factorUnits = FactorUnits.ofFactorUnitSpec(W, 8);
        Definition definition = Unit.definition("http://www.test.com/units#", factorUnits);
        Unit unit = definition.build();
        Assertions.assertTrue(unit.getUnitOfSystems().contains(Qudt.SystemsOfUnits.SI));
        Assertions.assertEquals("W⁸", unit.getSymbol().get());
        Assertions.assertEquals("W8", unit.getUcumCode().get());

        FactorUnits baseFactors = FactorUnits.ofFactorUnitSpec(KiloGM, 8, M, 16, SEC, -24);

        Unit baseUnit = Unit.definition("http://www.test.com/units#", baseFactors).doBuild();

        Assertions.assertTrue(unit.isConvertible(baseUnit));

        FactorUnits wrongFactors = FactorUnits.ofFactorUnitSpec(KiloGM, 8, M, 8, SEC, -8);

        Unit wongUnit = Unit.definition("http://www.test.com/units#", wrongFactors).doBuild();

        Assertions.assertFalse(unit.isConvertible(wongUnit));
    }

    @Test
    public void scaledUnitFromFactorUnits() {
        FactorUnits factorUnits = FactorUnits.ofFactorUnitSpec(KiloW, 2);
        Definition definition = Unit.definition("http://www.test.com/units#", factorUnits);
        Unit unit = definition.doBuild();
        FactorUnits baseFactors = FactorUnits.ofFactorUnitSpec(KiloGM, 2, M, 4, SEC, -6);

        Unit baseUnit = Unit.definition("http://www.test.com/units#", baseFactors).doBuild();

        Assertions.assertTrue(unit.isConvertible(baseUnit));

        Assertions.assertEquals(1000000, Qudt.convert(BigDecimal.ONE, unit, baseUnit).intValue());
    }

    @ParameterizedTest
    @MethodSource
    public void testParseUnit_specificCases(
            String input, QuantityKind quantityKind, Set<Unit> expected) {
        testParseUnit(input, quantityKind, expected);
    }

    @ParameterizedTest
    @MethodSource
    public void testParseUnit_allUnitsBySymbol(
            String input, QuantityKind quantityKind, Set<Unit> expected) {
        testParseUnit(input, quantityKind, expected);
    }

    private void testParseUnit(String input, QuantityKind quantityKind, Set<Unit> expected) {
        Set<Unit> result = Qudt.parseUnit(input, quantityKind);
        expected.forEach(
                u ->
                        assertTrue(
                                result.contains(u)
                                        || u.getExactMatches().stream()
                                                .anyMatch(equiv -> result.contains(equiv)),
                                String.format(
                                        "\ninput: '%s' (qk: %s): \nactual result [%s] does not contain expected %s (all expected: [%s])",
                                        input,
                                        Optional.ofNullable(quantityKind)
                                                .map(
                                                        qk ->
                                                                Qudt.NAMESPACES.quantityKind
                                                                        .abbreviate(qk.getIri()))
                                                .orElse("[null]"),
                                        result.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining(",")),
                                        u.getIriAbbreviated(),
                                        expected.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining(",")))));
        result.forEach(
                u ->
                        assertTrue(
                                expected.contains(u)
                                        || u.getExactMatches().stream()
                                                .anyMatch(equiv -> expected.contains(equiv)),
                                String.format(
                                        "\ninput: '%s' (qk: %s): \nexpected result [%s] does not contain actual %s (all actual: [%s])",
                                        input,
                                        Optional.ofNullable(quantityKind)
                                                .map(
                                                        qk ->
                                                                Qudt.NAMESPACES.quantityKind
                                                                        .abbreviate(qk.getIri()))
                                                .orElse("[null]"),
                                        expected.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining(",")),
                                        u.getIriAbbreviated(),
                                        result.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining(",")))));
    }

    public static Stream<Arguments> testParseUnit_specificCases() {
        return Stream.of(
                Arguments.of("m", QuantityKinds.Length, Set.of(Units.M)),
                Arguments.of("/h", QuantityKinds.Frequency, Set.of(Units.PER__HR)),
                Arguments.of("1/h", QuantityKinds.Frequency, Set.of(Units.PER__HR)),
                Arguments.of("L", QuantityKinds.LiquidVolume, Set.of(Units.L)),
                Arguments.of("Pascal", QuantityKinds.Pressure, Set.of(Units.PA)),
                Arguments.of("PASCAL", QuantityKinds.Pressure, Set.of(Units.PA)),
                Arguments.of("Watt/m2K", QuantityKinds.ThermalAdmittance, Set.of(W__PER__M2__K)),
                Arguments.of("W/m2K", QuantityKinds.ThermalAdmittance, Set.of(W__PER__M2__K)),
                Arguments.of("W/m2Kelvin", QuantityKinds.ThermalAdmittance, Set.of(W__PER__M2__K)),
                Arguments.of("Watt/m^2K", QuantityKinds.ThermalAdmittance, Set.of(W__PER__M2__K)),
                Arguments.of("Pa", QuantityKinds.Pressure, Set.of(Units.PA)),
                Arguments.of("km/h", QuantityKinds.LinearVelocity, Set.of(Units.KiloM__PER__HR)),
                Arguments.of(
                        "m3/kg",
                        Units.M3__PER__KiloGM.getQuantityKinds().stream().findFirst().get(),
                        Set.of(Units.M3__PER__KiloGM)),
                Arguments.of("kV", QuantityKinds.Voltage, Set.of(Units.KiloV)),
                Arguments.of("kV*A", QuantityKinds.ComplexPower, Set.of(Units.KiloV__A)),
                Arguments.of("kV.A", QuantityKinds.ComplexPower, Set.of(Units.KiloV__A)),
                Arguments.of("kg/m^3", QuantityKinds.Density, Set.of(Units.KiloGM__PER__M3)),
                Arguments.of("kg per m3", QuantityKinds.Density, Set.of(Units.KiloGM__PER__M3)),
                Arguments.of("kg.m-3", QuantityKinds.Density, Set.of(Units.KiloGM__PER__M3)),
                Arguments.of("kgm⁻³", QuantityKinds.Density, Set.of(Units.KiloGM__PER__M3)),
                Arguments.of(
                        "m²·Kelvin/Watt",
                        M2__K__PER__W.getQuantityKinds().stream().findFirst().get(),
                        Set.of(M2__K__PER__W)),
                Arguments.of(
                        "Watt/m2 Kelvin",
                        QuantityKinds.CoefficientOfHeatTransfer,
                        Set.of(Units.W__PER__M2__K)),
                Arguments.of("m4", QuantityKinds.SecondMomentOfArea, Set.of(Units.M4)),
                Arguments.of("kN*m2", QuantityKinds.WarpingMoment, Set.of(KiloN__M2)),
                Arguments.of(
                        "kg / Pa s m",
                        QuantityKinds.VaporPermeability,
                        Set.of(KiloGM__PER__PA__SEC__M)),
                Arguments.of(
                        "kg.Pa-1.s-1.m-1",
                        QuantityKinds.VaporPermeability,
                        Set.of(KiloGM__PER__PA__SEC__M)),
                Arguments.of(
                        "kg / s m Pascal",
                        QuantityKinds.VaporPermeability,
                        Set.of(KiloGM__PER__PA__SEC__M)),
                Arguments.of(
                        "Nm/(m*rad)",
                        QuantityKinds.ModulusOfRotationalSubgradeReaction,
                        Set.of(N__M__PER__M__RAD)),
                Arguments.of("°C", QuantityKinds.Temperature, Set.of(Units.DEG_C)),
                Arguments.of("°F", QuantityKinds.Temperature, Set.of(Units.DEG_F)),
                Arguments.of("γ", QuantityKinds.MagneticField, Set.of(Units.GAMMA)),
                Arguments.of("Å", QuantityKinds.Length, Set.of(Units.ANGSTROM)),
                Arguments.of(
                        "cycles/s", QuantityKinds.RotationalFrequency, Set.of(Units.CYC__PER__SEC)),
                Arguments.of("1/s", QuantityKinds.CountRate, Set.of(PER__SEC)),
                Arguments.of("m3/h", QuantityKinds.MoistureDiffusivity, Set.of(M3__PER__HR)),
                Arguments.of(
                        "mol/L",
                        QuantityKinds.AmountOfSubstanceIonConcentration,
                        Set.of(MOL__PER__L)),
                Arguments.of("m2/s", QuantityKinds.KinematicViscosity, Set.of(M2__PER__SEC)),
                Arguments.of("rad/m", QuantityKinds.Curvature, Set.of(RAD__PER__M)),
                Arguments.of("m2K/W", QuantityKinds.ThermalInsulance, Set.of(Units.M2__K__PER__W)),
                Arguments.of(
                        "W/m K", QuantityKinds.ThermalConductivity, Set.of(Units.W__PER__M__K)),
                Arguments.of(
                        "cd/klm",
                        QuantityKinds.LuminousIntensityDistribution,
                        Set.of(Units.CD__PER__KiloLM)));
    }

    public static Stream<Arguments> testParseUnit_allUnitsBySymbol() {
        List<Map<QuantityKind, Set<Unit>>> all =
                Qudt.allUnits().stream()
                        .filter(not(Unit::isDeprecated))
                        .filter(u -> u.getSymbol().isPresent())
                        .filter(u -> !u.equals(UNKNOWN))
                        .collect(Collectors.groupingBy(u -> u.getSymbol().get()))
                        .entrySet()
                        .stream()
                        .map(
                                e -> {
                                    List<Unit> unitsWithSameSymbol = e.getValue();
                                    Set<QuantityKind> quantityKinds =
                                            unitsWithSameSymbol.stream()
                                                    .flatMap(u -> u.getQuantityKinds().stream())
                                                    .collect(Collectors.toSet());
                                    Map<QuantityKind, Set<Unit>> expectedUnitsForQuantityKind =
                                            new HashMap<>();
                                    for (QuantityKind selectedQuantityKind : quantityKinds) {
                                        expectedUnitsForQuantityKind.put(
                                                selectedQuantityKind,
                                                unitsWithSameSymbol.stream()
                                                        .filter(
                                                                u ->
                                                                        u.getQuantityKinds()
                                                                                .contains(
                                                                                        selectedQuantityKind))
                                                        .collect(Collectors.toSet()));
                                    }
                                    return expectedUnitsForQuantityKind;
                                })
                        .toList();
        return all.stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(
                        entry -> {
                            Set<Unit> expectedUnits = entry.getValue();
                            QuantityKind selectedQuantityKind = entry.getKey();
                            return Arguments.of(
                                    expectedUnits.stream()
                                            .findFirst()
                                            .get()
                                            .getSymbol()
                                            .orElse("[no symbol]"),
                                    selectedQuantityKind,
                                    expectedUnits);
                        });
    }

    private static boolean unitsHaveSameQuantityKinds(Unit left, Unit right) {
        Set<QuantityKind> leftQK = left.getQuantityKinds();
        Set<QuantityKind> rightQK = right.getQuantityKinds();
        return leftQK.equals(rightQK);
    }

    @ParameterizedTest
    @MethodSource
    public void testBestMatchForFactorUnitsComparator(
            FactorUnits factorUnits, Unit left, Unit right, int expectedResultSign) {
        Comparator<Unit> cmp = Qudt.bestMatchForFactorUnitsComparator(factorUnits);
        int result = cmp.compare(left, right);
        float expectedResultSignF = Math.signum(expectedResultSign); // make sure
        float resultSignF = Math.signum(result);
        if (expectedResultSignF == 0.0f) {
            Assertions.assertEquals(
                    expectedResultSignF,
                    resultSignF,
                    "factor units %s\n%s should equally good matches for %s, but %s is wrongly chosen to be better"
                            .formatted(
                                    factorUnits.toString(),
                                    left,
                                    right,
                                    resultSignF < 0 ? left : right));
        } else {
            Unit expectedBetter = null;
            Unit expectedWorse = null;
            if (expectedResultSignF == 1.0f) {
                expectedBetter = right;
                expectedWorse = left;
            } else if (expectedResultSignF == -1.0f) {
                expectedBetter = left;
                expectedWorse = right;
            } else {
                fail("Illegal value provided for expectedResultSign: " + expectedResultSign);
            }
            if (resultSignF == 0.0) {
                Assertions.assertEquals(
                        Math.signum(expectedResultSign),
                        Math.signum(result),
                        "factor units %s\n%s should be a better match than %s, but they are wrongly assessed as equally good matches"
                                .formatted(factorUnits.toString(), expectedBetter, expectedWorse));
            } else {
                Assertions.assertEquals(
                        Math.signum(expectedResultSign),
                        Math.signum(result),
                        "factor units %s\n%s should be a better match than %s, but the inverse is the case"
                                .formatted(factorUnits.toString(), expectedBetter, expectedWorse));
            }
        }
    }

    public static Stream<Arguments> testBestMatchForFactorUnitsComparator() {
        return Stream.of(
                Arguments.of(FactorUnits.ofFactorUnitSpec(M, -3), PER__M3, PER__L, -1),
                Arguments.of(FactorUnits.ofFactorUnitSpec(M3, -1), PER__M3, PER__L, -1),
                Arguments.of(FactorUnits.ofFactorUnitSpec(L, -1), PER__M3, PER__L, 1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M, 1, SEC, -2, M, -1),
                        N__PER__M,
                        J__PER__M2,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M, -3),
                        KiloGM__PER__M3,
                        GM__PER__DeciM3,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M, -3),
                        KiloGM__PER__M3,
                        GM__PER__L,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M, -3),
                        GM__PER__DeciM3,
                        GM__PER__L,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M3, -1),
                        KiloGM__PER__M3,
                        GM__PER__DeciM3,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M3, -1),
                        KiloGM__PER__M3,
                        GM__PER__L,
                        -1),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(KiloGM, 1, M3, -1),
                        GM__PER__DeciM3,
                        GM__PER__L,
                        -1));
    }
}
