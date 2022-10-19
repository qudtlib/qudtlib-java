package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
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
                        FactorUnitSelection.fromFactorUnitSpec(
                                Qudt.Units.KiloGM, 1, Qudt.Units.M, -3),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.KiloGM__PER__M3.matches(
                        FactorUnitSelection.fromFactorUnitSpec(Qudt.Units.GM, 1, Qudt.Units.M, -3),
                        FactorUnitMatchingMode.ALLOW_SCALED));
    }

    @Test
    public void testMatchingModeExact() {
        assertFalse(Qudt.Units.GM__PER__DeciM3.matches(Qudt.Units.KiloGM, 1, Qudt.Units.M, -3));
        assertFalse(Qudt.Units.KiloGM__PER__M3.matches(Qudt.Units.GM, 1, Qudt.Units.M, -3));
    }

    @Test
    public void testSearchModeExactOnlyOne() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT_ONLY_ONE, Qudt.Units.N, 1, Qudt.Units.M, 1);
        assertEquals(1, units.size());
        assertTrue(units.contains(Qudt.Units.J));
    }

    @Test
    public void testSearchModeExact_2Results() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.EXACT, Qudt.Units.N, 1, Qudt.Units.M, 1);
        assertEquals(2, units.size());
        assertTrue(units.contains(Qudt.Units.J));
        assertTrue(units.contains(Qudt.Units.N__M));
    }

    @Test
    public void testSearchModeBestEffortOnlyOne() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_EFFORT_ONLY_ONE, "KiloGM", 1, "M", -3);
        assertEquals(1, units.size());
        assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.BEST_EFFORT_ONLY_ONE, "KiloN", 1, "MilliM", 1);
        assertEquals(1, units.size());
        assertTrue(units.contains(Qudt.Units.J));
    }

    @Test
    public void testSearchModeAllowScaled() {
        Set<Unit> units =
                Qudt.derivedUnitsFromUnitExponentPairs(
                        DerivedUnitSearchMode.ALLOW_SCALED, "KiloGM", 1, "M", -3);
        assertEquals(2, units.size());
        assertTrue(units.contains(Qudt.Units.KiloGM__PER__M3));
        assertTrue(units.contains(Qudt.Units.GM__PER__DeciM3));
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
        assertFalse(
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
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.EXACT));
        factors = new Object[] {Qudt.Units.M, 1, Qudt.Units.KiloN, 1};
        assertTrue(Qudt.Units.KiloN__M.matches(FactorUnitSelection.fromFactorUnitSpec(factors)));
        factors = new Object[] {Qudt.Units.KiloM, 1, Qudt.Units.N, 1};
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.EXACT));
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
        assertFalse(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        factors =
                new Object[] {
                    Qudt.Units.SEC, -2, Qudt.Units.TONNE, 1, Qudt.Units.M, 1, Qudt.Units.M, 1
                };
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertTrue(
                Qudt.Units.KiloJ.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.MilliOHM.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.MilliS.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));

        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        assertFalse(
                Qudt.Units.W__PER__K.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.V__PER__K.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
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
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
    }

    @Test
    public void test_squareInNominator() {
        Object[] factors = new Object[] {Qudt.Units.MilliM, 2, Qudt.Units.SEC, -1};
        assertTrue(
                Qudt.Units.MilliM2__PER__SEC.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        factors = new Object[] {Qudt.Units.KiloGM, 2, Qudt.Units.SEC, -2};
        assertTrue(
                Qudt.Units.KiloGM2__PER__SEC2.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
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
        assertTrue(
                Qudt.Units.N__PER__M2.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(Qudt.Units.N__PER__M2.matches(FactorUnitSelection.fromFactorUnitSpec(factors)));
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
        assertTrue(
                Qudt.Units.N__PER__M2.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.N__PER__M2.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.EXACT));
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
                        Qudt.Units.KiloN__M.matches(
                                FactorUnitSelection.fromFactorUnitSpec(factors),
                                FactorUnitMatchingMode.ALLOW_SCALED),
                        () -> "failed for " + factorUnits);
                assertTrue(
                        Qudt.Units.KiloN__M.matches(
                                FactorUnitSelection.fromFactorUnitSpec(factors)),
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
                Qudt.Units.KiloN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED),
                () -> "failed for " + factorUnits);
        assertTrue(
                Qudt.Units.KiloJ.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED),
                () -> "failed for " + factorUnits);
        assertFalse(
                Qudt.Units.MilliOHM.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED),
                () -> "failed for " + factorUnits);
        assertFalse(
                Qudt.Units.MilliS.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED),
                () -> "failed for " + factorUnits);
        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        assertFalse(
                Qudt.Units.W__PER__K.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.V__PER__K.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
    }

    @Test
    public void testMilliJ() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.MilliM, 1
                };
        assertTrue(
                Qudt.Units.MilliN__M.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertFalse(
                Qudt.Units.MilliH__PER__KiloOHM.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
        assertTrue(
                Qudt.Units.MilliJ.matches(
                        FactorUnitSelection.fromFactorUnitSpec(factors),
                        FactorUnitMatchingMode.ALLOW_SCALED));
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
        assertTrue(
                Qudt.derivedUnitsFromFactorUnits(DerivedUnitSearchMode.EXACT, simplified)
                        .contains(Qudt.Units.N__PER__M2));
        assertTrue(
                Qudt.derivedUnitsFromFactorUnits(DerivedUnitSearchMode.EXACT, simplified)
                        .contains(Qudt.Units.PA));
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
}
