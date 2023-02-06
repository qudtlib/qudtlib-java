package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.algorithm.AssignmentProblem;
import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        Assertions.assertTrue(length.hasLabel("Length"));
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
                Qudt.derivedUnitsFromMap(DerivedUnitSearchMode.BEST_MATCH, Map.of(Qudt.Units.M, -3))
                        .contains(Qudt.Units.PER__M3));
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH,
                                Qudt.Units.MilliA,
                                1,
                                Qudt.Units.IN,
                                -1)
                        .contains(Qudt.Units.MilliA__PER__IN));
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
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
        Assertions.assertEquals(Qudt.Units.PA, Qudt.unitFromLabelRequired("Pascal"));
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
                        Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M));
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.GM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.GM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.MOL,
                        1,
                        Qudt.Units.M,
                        -2,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.MOL__PER__M2__SEC));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
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
                Qudt.derivedUnitsFromUnitExponentPairs(
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
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 1);
        Assertions.assertTrue(units.contains(Qudt.Units.M));
        Assertions.assertFalse(units.contains(Qudt.Units.RAD)); // m per m should not match here!
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.A, -1);
        Assertions.assertEquals(0, units.size());
    }

    @Test
    public void testDerivedUnit() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.K, -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByIri() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.K.getIri(), -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M.getIri(), -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByLocalname() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "K", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "M", -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByLabel() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "Meter", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "Metre", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "Kelvin", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.BEST_MATCH, "Bar", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__BAR));
    }

    @Test
    public void testDerivedUnit2() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.M, 1, Qudt.Units.N, 1);
        Assertions.assertTrue(units.contains(Qudt.Units.N__M));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.scale("Kilo", "Gram"),
                        1,
                        Qudt.Units.M,
                        -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.N, 1, Qudt.Units.M, -2);
        Assertions.assertTrue(units.contains(Qudt.Units.N__PER__M2));
        Assertions.assertEquals(1, units.size());
    }

    @Test
    public void testDerivedUnit3() {
        // test making sure overspecifying factors are rejected
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.BEST_MATCH,
                                Qudt.Units.M,
                                1,
                                Qudt.Units.N,
                                1,
                                Qudt.Units.SEC,
                                -2)
                        .isEmpty());
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
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
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
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
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        -2);
        Assertions.assertTrue(units.contains(Qudt.Units.N__PER__M2));
    }

    @Test
    public void testDerivedUnit5() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
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
                Qudt.derivedUnitsFromUnitExponentPairs(
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
        Assertions.assertTrue(units.contains(Qudt.Units.N__M__PER__M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH,
                        Qudt.Units.M,
                        2,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        -2);
        Assertions.assertTrue(units.contains(Qudt.Units.N__M__PER__M2));
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
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.KiloN, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 1)));
    }

    @Test
    public void testGetUnitFactorsUnscaled() {
        Unit unit = Qudt.Units.KiloN__M;
        List<FactorUnit> unitFactors = Qudt.factorUnits(unit);
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.KiloN, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 1)));
        unitFactors = Qudt.unscale(unitFactors);
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.N, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 1)));
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
                Matchers.comparesEqualTo(new BigDecimal("0.2641720372841846541406853467997671")));
    }

    @Test
    public void testConvert_Celsius_to_Fahrenheit() {
        QuantityValue celsius100 =
                new QuantityValue(new BigDecimal("100"), Qudt.unitFromLocalnameRequired("DEG_C"));
        QuantityValue fahrenheit = Qudt.convert(celsius100, Qudt.unitIriFromLocalname("DEG_F"));
        Assertions.assertNotNull(fahrenheit);
        MatcherAssert.assertThat(
                fahrenheit.getValue(),
                Matchers.comparesEqualTo(new BigDecimal("212.0003929999999462664000000000043")));
        Assertions.assertEquals(Qudt.unitIriFromLocalname("DEG_F"), fahrenheit.getUnit().getIri());
    }

    @Test
    public void testConvert_Celsius_to_Fahrenheit_2() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("100"), Units.DEG_C, Units.DEG_F),
                Matchers.comparesEqualTo(new BigDecimal("212.0003929999999462664000000000043")));
    }

    @Test
    public void testConvert_Fahrenheit_to_Celsius() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("100"), Units.DEG_F, Units.DEG_C),
                Matchers.comparesEqualTo(new BigDecimal("37.7775594444444693186492")));
    }

    @Test
    public void testConvert_byte_to_megabyte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1000000"), Units.BYTE, Units.MegaBYTE),
                Matchers.comparesEqualTo(new BigDecimal("1.000000000000000446394706347217183")));
    }

    @Test
    public void testConvert_megabyte_to_byte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1"), Units.MegaBYTE, Units.BYTE),
                Matchers.comparesEqualTo(new BigDecimal("999999.9999999995536052936527830164")));
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
    public void scoreOverlapMatrixTest() {
        Unit va = Qudt.Units.V__A;
        List<List<FactorUnit>> factorCombinations = va.getAllPossibleFactorUnitCombinations();
        double[][] mat = Qudt.getUnitSimilarityMatrix(factorCombinations, factorCombinations);
        double score = AssignmentProblem.instance(mat).solve().getWeight();
        Assertions.assertEquals(0.0, score, 0.000001);
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
                            {Units.GM, 1, Units.M, 2, Units.SEC, -2},
                            {Units.N, 1, Units.M, 1}
                        }),
                Arguments.of(
                        1,
                        Qudt.Units.N,
                        new Object[][] {
                            {Units.N, 1},
                            {Units.GM, 1, Units.M, 1, Units.SEC, -2}
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
                                Qudt.Units.GM, 1,
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
                                Qudt.Units.GM, 1,
                                Qudt.Units.SEC, -2
                            },
                        }),
                Arguments.of(
                        4,
                        Units.J__PER__KiloGM__K__PA,
                        new Object[][] {
                            {Units.J__PER__KiloGM__K__PA, 1},
                            {Units.J, 1, Units.GM, -1, Units.K, -1, Units.PA, -1},
                            {Units.N, 1, Units.M, 1, Units.GM, -1, Units.K, -1, Units.PA, -1},
                            {
                                Units.GM, 1, Units.M, 2, Units.SEC, -2, Units.GM, -1, Units.K, -1,
                                Units.PA, -1,
                            },
                            {Units.M, 2, Units.SEC, -2, Units.K, -1, Units.PA, -1},
                            {Units.J, 1, Units.GM, -1, Units.K, -1, Units.N, -1, Units.M, 2},
                            {
                                Units.J, 1, Units.GM, -2, Units.K, -1, Units.M, -1, Units.SEC, 2,
                                Units.M, 2,
                            },
                            {Units.J, 1, Units.GM, -2, Units.K, -1, Units.M, 1, Units.SEC, 2},
                            {Units.N, 1, Units.M, 3, Units.GM, -1, Units.K, -1, Units.N, -1},
                            {
                                Units.N, 1, Units.M, 3, Units.GM, -2, Units.K, -1, Units.M, -1,
                                Units.SEC, 2,
                            },
                            {
                                Units.GM, 1, Units.M, 4, Units.SEC, -2, Units.GM, -1, Units.K, -1,
                                Units.N, -1,
                            },
                            {Units.M, 4, Units.SEC, -2, Units.K, -1, Units.N, -1},
                            {
                                Units.GM, 1, Units.M, 4, Units.SEC, -2, Units.GM, -2, Units.K, -1,
                                Units.M, -1, Units.SEC, 2,
                            },
                            {Units.GM, -1, Units.M, 3, Units.K, -1},
                            {Units.N, 1, Units.M, 2, Units.GM, -2, Units.K, -1, Units.SEC, 2},
                            {
                                Units.M, 3, Units.GM, 1, Units.SEC, -2, Units.GM, -2, Units.K, -1,
                                Units.SEC, 2,
                            },
                        }));
    }
}
