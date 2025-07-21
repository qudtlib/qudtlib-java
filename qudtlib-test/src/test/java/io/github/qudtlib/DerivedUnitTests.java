package io.github.qudtlib;

import static io.github.qudtlib.model.Units.SR;
import static io.github.qudtlib.model.Units.W;
import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

/**
 * Tests focusing on finding units by factor units.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class DerivedUnitTests {
    @Test
    public void testSingleFactorUnit() {
        Unit du = Qudt.Units.PER__M;
        assertTrue(du.matches(Qudt.Units.M, -1));
        assertFalse(du.matches(Qudt.Units.M, -1, Qudt.Units.KiloGM, 1));
        assertFalse(du.matches(Qudt.Units.KiloGM, -1));
        assertFalse(du.matches(Qudt.Units.SEC, -2));
        assertTrue(Qudt.Units.M.matches(Qudt.Units.M, 1));
    }

    @Test
    public void testTwoFactorUnit() {
        Unit du = Qudt.Units.KiloGM__PER__M2;
        assertTrue(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 1));
        assertTrue(du.matches(Qudt.Units.KiloGM, 1, Qudt.Units.M, -2));
        assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        assertFalse(du.matches(Qudt.Units.M, -2));
        assertFalse(du.matches(Qudt.Units.KiloGM, 1));
    }

    @Test
    public void testDeepFactorUnit() {
        Unit du = Qudt.Units.N__PER__KiloGM;
        assertTrue(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2));
        assertTrue(du.matches(Qudt.Units.N, 1, Qudt.Units.KiloGM, -1));
        assertFalse(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.N,
                        1));
        assertFalse(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.N,
                        1,
                        Qudt.Units.KiloGM,
                        -1));
        assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        assertFalse(du.matches(Qudt.Units.M, -2));
        assertFalse(du.matches(Qudt.Units.KiloGM, 1));
    }

    @Test
    public void testMatchingModeAllowScaled() {
        assertTrue(
                Qudt.Units.GM__PER__DeciM3.matches(
                        FactorUnits.ofFactorUnitSpec(Qudt.Units.KiloGM, 1, Qudt.Units.M, -3)));
        assertFalse(
                Qudt.Units.KiloGM__PER__M3.matches(
                        FactorUnits.ofFactorUnitSpec(Qudt.Units.GM, 1, Qudt.Units.M, -3)));
    }

    @Test
    public void testMatchingModeExact() {
        assertTrue(Qudt.Units.GM__PER__DeciM3.matches(Qudt.Units.KiloGM, 1, Qudt.Units.M, -3));
        assertFalse(Qudt.Units.GM__PER__DeciM3.matches(Qudt.Units.KiloGM, 1, Qudt.Units.DeciM, -3));
        assertTrue(Qudt.Units.GM__PER__DeciM3.matches(Qudt.Units.GM, 1, Qudt.Units.DeciM, -3));
        assertFalse(Qudt.Units.KiloGM__PER__M3.matches(Qudt.Units.GM, 1, Qudt.Units.M, -3));
        assertFalse(Qudt.Units.KiloGM__PER__M3.matches(Qudt.Units.KiloGM, 1, Qudt.Units.DeciM, -3));
        assertTrue(Qudt.Units.KiloGM__PER__M3.matches(Qudt.Units.KiloGM, 1, Qudt.Units.M, -3));
    }

    @Test
    public void testSearchModeExactOnlyOne() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, Qudt.Units.N, 1, Qudt.Units.M, 1);
        assertEquals(1, units.size());
        assertEquals(Qudt.Units.J, units.stream().findFirst().get());
    }

    @Test
    public void testSearchModeExact_2Results() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.ALL, Qudt.Units.N, 1, Qudt.Units.M, 1);
        assertTrue(units.contains(Qudt.Units.J));
        assertTrue(units.contains(Qudt.Units.N__M));
        assertTrue(units.contains(Qudt.Units.N__M__PER__RAD));
        assertTrue(units.contains(Qudt.Units.W__SEC));
        assertEquals(4, units.size());
    }

    @Test
    public void testSearchModeBestMatch() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "KiloGM", 1, "M", -3);
        assertEquals(1, units.size());
        assertEquals(Qudt.Units.KiloGM__PER__M3, units.stream().findFirst().get());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "KiloN", 1, "MilliM", 1);
        assertEquals(1, units.size());
        assertEquals(Qudt.Units.J, units.stream().findFirst().get());
        units =
                Qudt.unitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_MATCH, "KiloGM", 1, "M", 1, "SEC", -2, "M", -1);
        assertEquals(1, units.size());
        assertEquals(Qudt.Units.N__PER__M, units.stream().findFirst().get());
    }

    @Test
    public void testSearchModeAllowScaled() {
        List<Unit> units =
                Qudt.unitsFromUnitExponentPairs(DerivedUnitSearchMode.ALL, "KiloGM", 1, "M", -3);
        assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        assertTrue(units.contains(Qudt.Units.GM__PER__DeciM3));
        assertTrue(units.contains(Qudt.Units.GM__PER__L));
        assertTrue(units.contains(Qudt.Units.MilliGM__PER__MilliL));
        assertEquals(4, units.size());
    }

    @Test
    public void testDeepFactorUnitWithDuplicateUnitExponentCombination() {
        Unit du = Qudt.Units.N__M__PER__KiloGM;
        boolean matches = du.matches(Qudt.Units.N, 1, Qudt.Units.KiloGM, -1, Qudt.Units.M, 1);
        assertTrue(matches);
        assertFalse(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.N,
                        1));
        assertFalse(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.N,
                        1));
        assertFalse(
                du.matches(
                        Qudt.Units.KiloGM,
                        -1,
                        Qudt.Units.M,
                        1,
                        Qudt.Units.KiloGM,
                        1,
                        Qudt.Units.SEC,
                        -2,
                        Qudt.Units.N,
                        1,
                        Qudt.Units.KiloGM,
                        -1));
        assertTrue(
                du.matches(
                        Qudt.Units.KiloGM, 1,
                        Qudt.Units.M, 1,
                        Qudt.Units.SEC, -2,
                        Qudt.Units.M, 1,
                        Qudt.Units.KiloGM, -1));
        assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        assertFalse(du.matches(Qudt.Units.M, -2));
        assertFalse(du.matches(Qudt.Units.KiloGM, 1));
        assertFalse(du.matches(Qudt.Units.N, 1, Qudt.Units.KiloGM, -1));
    }

    @Test
    public void
            testDeepFactorUnitWithDuplicateUnitExponentCombination_matchWithAggregatedExpression() {
        Unit du = Qudt.Units.N__M__PER__KiloGM;
        // now simplify: aggregate the M^1, M^1 to M^2: should still work.
        assertTrue(
                du.matches(
                        Qudt.Units.KiloGM, 1,
                        Qudt.Units.M, 2,
                        Qudt.Units.SEC, -2,
                        Qudt.Units.KiloGM, -1));
        // now simplify: wrongly aggregate the KiloGM^1, KiloGM^-1 to KiloGM^0: should not work
        assertTrue(
                du.matches(
                        Qudt.Units.M, 2,
                        Qudt.Units.SEC, -2,
                        Qudt.Units.KiloGM, 0));
    }

    @Test
    public void testScaledFactors() {
        // mJoule =
        //               new IfcDerivedUnit(100, IfcUnitType.ENERGYUNIT, Map.of(kg, 1, sec, -2, km,
        // 2), false);
        Object[] factors =
                new Object[] {
                    Qudt.Units.SEC, -2, Qudt.Units.KiloGM, 1, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors = new Object[] {Qudt.Units.M, 1, Qudt.Units.KiloN, 1};
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors = new Object[] {Qudt.Units.KiloM, 1, Qudt.Units.N, 1};
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors =
                new Object[] {
                    Qudt.Units.SEC,
                    -2,
                    Qudt.Units.KiloGM,
                    1,
                    Qudt.Units.KiloM,
                    1,
                    Qudt.Units.KiloM,
                    1
                };
        assertFalse(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors =
                new Object[] {
                    Qudt.Units.SEC, -2, Qudt.Units.TONNE, 1, Qudt.Units.M, 1, Qudt.Units.M, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertTrue(Qudt.Units.KiloJ.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertFalse(Qudt.Units.MilliOHM.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertFalse(Qudt.Units.MilliS.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        assertFalse(Qudt.Units.W__PER__K.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertFalse(Qudt.Units.V__PER__K.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void testScaledFactors_negExpFirst() {
        // mJoule =
        //               new IfcDerivedUnit(100, IfcUnitType.ENERGYUNIT, Map.of(kg, 1, sec, -2, km,
        // 2), false);
        Object[] factors =
                new Object[] {
                    Qudt.Units.SEC, -2, Qudt.Units.KiloGM, 1, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void test_squareInNominator() {
        Object[] factors = new Object[] {Qudt.Units.MilliM, 2, Qudt.Units.SEC, -1};
        assertTrue(Qudt.Units.MilliM2__PER__SEC.matches(FactorUnits.ofFactorUnitSpec(factors)));
        factors = new Object[] {Qudt.Units.KiloGM, 2, Qudt.Units.SEC, -2};
        assertTrue(Qudt.Units.KiloGM2__PER__SEC2.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void test_squareInDenominator() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.M, 1, Qudt.Units.M, -2, Qudt.Units.SEC, -2
                };
        assertTrue(Qudt.Units.N__PER__M2.matches(factors));
        factors =
                new Object[] {
                    Qudt.Units.M, -2, Qudt.Units.SEC, -2, Qudt.Units.KiloGM, 1, Qudt.Units.M, 1
                };
        assertTrue(Qudt.Units.N__PER__M2.matches(factors));
    }

    @Test
    public void test_withAndWithoutRatioOfSameUnits() {
        Object[] factors = new Object[] {Qudt.Units.M, 2, SR, 1};
        assertTrue(Qudt.Units.M2.matches(factors));
        factors = new Object[] {Qudt.Units.M, 2};
        assertTrue(Qudt.Units.M2__SR.matches(factors));
    }

    @Test
    public void testScale_squareInDenominator1() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM,
                    1,
                    Qudt.Units.M,
                    1,
                    Qudt.Units.MilliM,
                    -2,
                    Qudt.Units.KiloSEC,
                    -2
                };
        assertTrue(Qudt.Units.N__PER__M2.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertTrue(Qudt.Units.N__PER__M2.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void testGM() {
        assertEquals(
                FactorUnits.ofFactorUnitSpec(new BigDecimal(0.001), Units.KiloGM, 1),
                Units.GM.normalize());
    }

    @Test
    public void testScale_squareInDenominator2() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.GM,
                    1,
                    Qudt.Units.MilliM,
                    1,
                    Qudt.Units.M,
                    -2,
                    Qudt.Units.MilliSEC,
                    -2
                };
        assertTrue(Qudt.Units.N__PER__M2.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void testScaledFactorsWithChangingOrder() {
        // mJoule =
        //               new IfcDerivedUnit(100, IfcUnitType.ENERGYUNIT, Map.of(kg, 1, sec, -2, km,
        // 2), false);
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        List<List<FactorUnit>> successfulFor = new ArrayList<>();
        List<FactorUnit> factorUnits = new ArrayList<>();
        for (int i = 0; i < factors.length; i += 2) {
            factorUnits.add(new FactorUnit((Unit) factors[i], (Integer) factors[i + 1]));
        }
        try {
            for (int i = 0; i < 20; i++) {
                Collections.shuffle(factorUnits);
                factors =
                        factorUnits.stream()
                                .flatMap(fu -> Stream.of(fu.getUnit(), fu.getExponent()))
                                .toArray();
                assertTrue(
                        Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)),
                        () -> "failed for " + factorUnits);
                assertTrue(
                        Qudt.Units.KiloJ.matches(FactorUnits.ofFactorUnitSpec(factors)),
                        () -> "failed for " + factorUnits);
                successfulFor.add(new ArrayList<>(factorUnits));
            }
        } catch (AssertionFailedError e) {
            System.err.println("test succeeded for: ");
            successfulFor.forEach(System.err::println);
            System.err.println("test failed for:");
            System.err.println(factorUnits);
            throw e;
        }
        assertTrue(
                Qudt.Units.KiloN__M.matches(FactorUnits.ofFactorUnitSpec(factors)),
                () -> "failed for " + factorUnits);
        assertTrue(
                Qudt.Units.KiloJ.matches(FactorUnits.ofFactorUnitSpec(factors)),
                () -> "failed for " + factorUnits);
        assertFalse(
                Qudt.Units.MilliOHM.matches(FactorUnits.ofFactorUnitSpec(factors)),
                () -> "failed for " + factorUnits);
        assertFalse(
                Qudt.Units.MilliS.matches(FactorUnits.ofFactorUnitSpec(factors)),
                () -> "failed for " + factorUnits);
        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        assertFalse(Qudt.Units.W__PER__K.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertFalse(Qudt.Units.V__PER__K.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void testMilliJ() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.MilliM, 1
                };
        assertTrue(Qudt.Units.MilliN__M.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertFalse(Qudt.Units.MilliH__PER__KiloOHM.matches(FactorUnits.ofFactorUnitSpec(factors)));
        assertTrue(Qudt.Units.MilliJ.matches(FactorUnits.ofFactorUnitSpec(factors)));
    }

    @Test
    public void testSimplifyFactorUnits() {
        List<FactorUnit> simplified =
                Qudt.simplifyFactorUnits(
                        List.of(
                                new FactorUnit(Qudt.Units.N, 1),
                                new FactorUnit(Qudt.Units.M, -1),
                                new FactorUnit(Qudt.Units.M, -1)));
        assertEquals(2, simplified.size());
        List<Unit> units = Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.BEST_MATCH, simplified);
        assertEquals(Qudt.Units.PA, units.stream().findFirst().get());
    }

    @Test
    public void testScaleToBaseUnit() {
        Map.Entry<Unit, BigDecimal> base = Qudt.scaleToBaseUnit(Qudt.Units.KiloM);
        assertEquals(Qudt.Units.M, base.getKey());
        MatcherAssert.assertThat(base.getValue(), Matchers.comparesEqualTo(new BigDecimal("1000")));
        base = Qudt.scaleToBaseUnit(Qudt.Units.M);
        MatcherAssert.assertThat(base.getValue(), Matchers.comparesEqualTo(BigDecimal.ONE));
        assertEquals(Qudt.Units.M, base.getKey());
    }

    @Test
    public void testWatt() {
        List<FactorUnit> wattFactors =
                FactorUnits.ofFactorUnitSpec(Qudt.Units.J, 1, Qudt.Units.SEC, -1).getFactorUnits();
        List<Unit> units = Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.BEST_MATCH, wattFactors);
        assertEquals(1, units.size());
        Unit bestMatch = units.stream().findFirst().get();
        assertEquals(
                Qudt.Units.W,
                bestMatch,
                "Expected %s but was %s"
                        .formatted(formatUnit(Qudt.Units.W), formatUnit(bestMatch)));
        wattFactors = FactorUnits.ofUnit(W).getFactorUnits();
        units = Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.BEST_MATCH, wattFactors);
        assertEquals(1, units.size());
        assertEquals(W, units.stream().findFirst().get());
    }

    private String formatUnit(Unit unit) {
        return """
                %s:
                dependents: %d
                %s
                """
                .formatted(unit.getIriLocalname(), unit.getDependents(), unit.getFactorUnits());
    }

    @ParameterizedTest
    @MethodSource("testDerivedUnits")
    public void testDerivedUnitsExpectedResultPresent(
            int id, DerivedUnitSearchMode searchMode, Object[] spec, Unit[] expectedResults) {
        List<Unit> actualResults =
                Qudt.unitsFromFactorUnits(
                        searchMode, FactorUnits.ofFactorUnitSpec(spec).getFactorUnits());
        Stream.of(expectedResults)
                .forEach(
                        exp ->
                                Assertions.assertTrue(
                                        actualResults.contains(exp),
                                        () ->
                                                String.format(
                                                        "Expected unit %s not contained in result %s",
                                                        exp.toString(), actualResults)));
    }

    @ParameterizedTest
    @MethodSource("testDerivedUnits")
    public void testDerivedUnitsCorrectNumberOfResults(
            int id, DerivedUnitSearchMode searchMode, Object[] spec, Unit[] expectedResults) {
        List<Unit> actualResults =
                Qudt.unitsFromFactorUnits(
                        searchMode, FactorUnits.ofFactorUnitSpec(spec).getFactorUnits());
        Assertions.assertEquals(expectedResults.length, actualResults.size());
    }

    @ParameterizedTest
    @MethodSource("testDerivedUnits")
    public void testDerivedUnitsActualResultExpected(
            int id, DerivedUnitSearchMode searchMode, Object[] spec, Unit[] expectedResults) {
        List<Unit> actualResults =
                Qudt.unitsFromFactorUnits(
                        searchMode, FactorUnits.ofFactorUnitSpec(spec).getFactorUnits());
        actualResults.stream()
                .forEach(
                        act ->
                                Assertions.assertTrue(
                                        Stream.of(expectedResults).anyMatch(exp -> exp.equals(act)),
                                        () ->
                                                String.format(
                                                        "Resulting unit %s not in  expected %s",
                                                        act.toString(),
                                                        Arrays.toString(expectedResults))));
    }

    public static Stream<Arguments> testDerivedUnits() {
        return Stream.of(
                Arguments.of(
                        2,
                        DerivedUnitSearchMode.BEST_MATCH,
                        new Object[] {
                            Qudt.Units.M,
                            2,
                            Qudt.Units.KiloGM,
                            1,
                            Qudt.Units.SEC,
                            -2,
                            Qudt.Units.M,
                            -2
                        },
                        new Unit[] {Units.N__PER__M}),
                Arguments.of(
                        1,
                        DerivedUnitSearchMode.BEST_MATCH,
                        new Object[] {Qudt.Units.KiloN, 1, Qudt.Units.MilliM, 1},
                        new Unit[] {Units.J}),
                Arguments.of(
                        1,
                        DerivedUnitSearchMode.BEST_MATCH,
                        new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.M3, -1},
                        new Unit[] {Units.KiloGM__PER__M3}));
    }
}
