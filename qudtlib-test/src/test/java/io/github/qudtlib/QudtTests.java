package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
                Qudt.derivedUnitsFromMap(DerivedUnitSearchMode.EXACT, Map.of(Qudt.Units.M, -3))
                        .contains(Qudt.Units.PER__M3));
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.EXACT,
                                Qudt.Units.MilliA,
                                1,
                                Qudt.Units.IN,
                                -1)
                        .contains(Qudt.Units.MilliA__PER__IN));
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.EXACT,
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
                                DerivedUnitSearchMode.EXACT, Qudt.Units.M));
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.GM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.GM__PER__M3));
        Assertions.assertEquals(1, units.size());
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT,
                        Qudt.Units.MOL,
                        1,
                        Qudt.Units.M,
                        -2,
                        Qudt.Units.SEC,
                        -1);
        Assertions.assertTrue(units.contains(Qudt.Units.MOL__PER__M2__SEC));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, 1);
        Assertions.assertTrue(units.contains(Qudt.Units.M));
        Assertions.assertFalse(units.contains(Qudt.Units.RAD)); // m per m should not match here!
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.KiloGM, 1, Qudt.Units.A, -1);
        Assertions.assertEquals(0, units.size());
    }

    @Test
    public void testDerivedUnit() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.K, -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByIri() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M.getIri(), 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M.getIri(), 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.K.getIri(), -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M.getIri(), -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByLocalname() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "M", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "M", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "K", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "M", -2);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__M2));
    }

    @Test
    public void testDerivedUnitByLabel() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "Meter", 3);
        Assertions.assertTrue(units.contains(Qudt.Units.M3));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "Metre", 2);
        Assertions.assertTrue(units.contains(Qudt.Units.M2));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "Kelvin", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__K));
        units = Qudt.derivedUnitsFromUnitExponentPairs(DerivedUnitSearchMode.EXACT, "Bar", -1);
        Assertions.assertTrue(units.contains(Qudt.Units.PER__BAR));
    }

    @Test
    public void testDerivedUnit2() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.M, 1, Qudt.Units.N, 1);
        Assertions.assertTrue(units.contains(Qudt.Units.N__M));
        Assertions.assertTrue(units.contains(Qudt.Units.J));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.KiloGM, 1, Qudt.Units.M, -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT,
                        Qudt.scaledUnit("Kilo", "Gram"),
                        1,
                        Qudt.Units.M,
                        -3);
        Assertions.assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.N, 1, Qudt.Units.M, -2);
        Assertions.assertTrue(units.contains(Qudt.Units.N__PER__M2));
        Assertions.assertTrue(units.contains(Qudt.Units.PA));
        Assertions.assertEquals(2, units.size());
    }

    @Test
    public void testDerivedUnit3() {
        // test making sure overspecifying factors are rejected
        Assertions.assertTrue(
                Qudt.derivedUnitsFromUnitExponentPairs(
                                DerivedUnitSearchMode.EXACT,
                                Qudt.Units.M,
                                1,
                                Qudt.Units.N,
                                1,
                                Qudt.Units.SEC,
                                -2)
                        .isEmpty());

        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
                        DerivedUnitSearchMode.EXACT,
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
        Unit unit = Qudt.scaledUnit("Nano", "Meter");
        Assertions.assertEquals(Qudt.Units.NanoM, unit);
        unit = Qudt.scaledUnit("Giga", "Hertz");
        Assertions.assertEquals(Qudt.Units.GigaHZ, unit);
        unit = Qudt.scaledUnit("Kilo", "Gram");
        Assertions.assertEquals(Qudt.Units.KiloGM, unit);
        unit = Qudt.scaledUnit("KILO", "GRAM");
        Assertions.assertEquals(Qudt.Units.KiloGM, unit);
        unit = Qudt.scaledUnit(Qudt.Prefixes.Nano, Qudt.Units.M);
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
        unitFactors = Qudt.unscaledFactorUnits(unitFactors);
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.N, 1)));
        Assertions.assertTrue(unitFactors.contains(new FactorUnit(Qudt.Units.M, 1)));
    }

    @Test
    public void testUnitless() {
        Assertions.assertEquals(
                new QuantityValue(new BigDecimal("1.1234"), Qudt.Units.UNITLESS),
                Qudt.convert(
                        new BigDecimal("1.1234"), Qudt.Units.KiloGM__PER__M3, Qudt.Units.UNITLESS));
        Assertions.assertEquals(
                new QuantityValue(new BigDecimal("1.1234"), Qudt.Units.KiloGM__PER__M3),
                Qudt.convert(
                        new BigDecimal("1.1234"), Qudt.Units.UNITLESS, Qudt.Units.KiloGM__PER__M3));
        Assertions.assertEquals(
                new QuantityValue(new BigDecimal("1.1234"), Qudt.Units.UNITLESS),
                Qudt.convert(new BigDecimal("1.1234"), Qudt.Units.UNITLESS, Qudt.Units.UNITLESS));
    }

    @Test
    public void testConvert_N_to_kN() {
        QuantityValue converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.N, Qudt.Units.KiloN);
        MatcherAssert.assertThat(
                converted.getValue(), Matchers.comparesEqualTo(new BigDecimal("0.001")));
    }

    @Test
    public void testInconvertible() {
        assertThrows(
                InconvertibleQuantitiesException.class,
                () -> Qudt.convert(BigDecimal.ONE, Qudt.Units.SEC, Qudt.Units.M));
    }

    @Test
    public void testConvert_L_to_GAL_US() {
        QuantityValue converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.L, Qudt.Units.GAL_US);
        MatcherAssert.assertThat(
                converted.getValue(),
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
                Qudt.convert(new BigDecimal("100"), Units.DEG_C, Units.DEG_F).getValue(),
                Matchers.comparesEqualTo(new BigDecimal("212.0003929999999462664000000000043")));
    }

    @Test
    public void testConvert_Fahrenheit_to_Celsius() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("100"), Units.DEG_F, Units.DEG_C).getValue(),
                Matchers.comparesEqualTo(new BigDecimal("37.7775594444444693186492")));
    }

    @Test
    public void testConvert_byte_to_megabyte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1048576"), Units.BYTE, Units.MegaBYTE).getValue(),
                Matchers.comparesEqualTo(new BigDecimal("1.000000000000000000000000000000003")));
    }

    @Test
    public void testConvert_megabyte_to_byte() {
        MatcherAssert.assertThat(
                Qudt.convert(new BigDecimal("1"), Units.MegaBYTE, Units.BYTE).getValue(),
                Matchers.comparesEqualTo(new BigDecimal("1048575.999999999999999999999999997")));
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
        QuantityValue converted =
                Qudt.convert(BigDecimal.ONE, Qudt.Units.FemtoGM, Qudt.Units.KiloGM);
        MatcherAssert.assertThat(
                converted.getValue(),
                Matchers.comparesEqualTo(new BigDecimal("0.000000000000000001")));
    }

    @Test
    public void testConvert_Metric_to_Imperial() {
        QuantityValue converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.LB, Qudt.Units.KiloGM);
        MatcherAssert.assertThat(
                converted.getValue(), Matchers.comparesEqualTo(new BigDecimal("0.45359237")));
        converted = Qudt.convert(BigDecimal.ONE, Qudt.Units.BTU_IT__PER__LB, Qudt.Units.J__PER__GM);
        MatcherAssert.assertThat(
                converted.getValue(), Matchers.comparesEqualTo(new BigDecimal("2.326")));
    }
}
