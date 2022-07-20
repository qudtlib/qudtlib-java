package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertFalse(du.matches(Qudt.Units.M, -1, Qudt.Units.KiloGM, 1));
        Assertions.assertFalse(du.matches(Qudt.Units.KiloGM, -1));
        Assertions.assertFalse(du.matches(Qudt.Units.SEC, -2));
    }

    @Test
    public void testTwoFactorUnit() {
        Unit du = Qudt.Units.KiloGM__PER__M2;
        assertTrue(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 1));
        assertTrue(du.matches(Qudt.Units.KiloGM, 1, Qudt.Units.M, -2));
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2));
        Assertions.assertFalse(du.matches(Qudt.Units.KiloGM, 1));
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
        assertTrue(
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
        Assertions.assertFalse(
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
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2));
        Assertions.assertFalse(du.matches(Qudt.Units.KiloGM, 1));
    }

    @Test
    public void testDeepFactorUnitWithDuplicateUnitExponentCombination() {
        Unit du = Qudt.Units.N__M__PER__KiloGM;
        boolean matches = du.matches(Qudt.Units.N, 1, Qudt.Units.KiloGM, -1, Qudt.Units.M, 1);
        assertTrue(matches);
        assertTrue(
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
        assertTrue(
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
        Assertions.assertFalse(
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
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2, Qudt.Units.KiloGM, 2));
        Assertions.assertFalse(du.matches(Qudt.Units.M, -2));
        Assertions.assertFalse(du.matches(Qudt.Units.KiloGM, 1));
        Assertions.assertFalse(du.matches(Qudt.Units.N, 1, Qudt.Units.KiloGM, -1));
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
        Assertions.assertFalse(
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
        assertTrue(Qudt.Units.KiloN__M.matches(factors));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(factors));
        assertTrue(Qudt.Units.KiloJ.matches(factors));
        Assertions.assertFalse(Qudt.Units.MilliOHM.matches(factors));
        Assertions.assertFalse(Qudt.Units.MilliS.matches(factors));

        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        Assertions.assertFalse(Qudt.Units.W__PER__K.matches(factors));
        Assertions.assertFalse(Qudt.Units.V__PER__K.matches(factors));
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
        assertTrue(Qudt.Units.KiloN__M.matches(factors));
        factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.KiloM, 1
                };
        assertTrue(Qudt.Units.KiloN__M.matches(factors));
    }

    @Test
    public void test_squareInNominator() {
        Object[] factors = new Object[] {Qudt.Units.MilliM, 2, Qudt.Units.SEC, -1};
        assertTrue(Qudt.Units.MilliM2__PER__SEC.matches(factors));
        factors = new Object[] {Qudt.Units.KiloGM, 2, Qudt.Units.SEC, -2};
        assertTrue(Qudt.Units.KiloGM2__PER__SEC2.matches(factors));
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
        assertTrue(Qudt.Units.N__PER__M2.matches(factors));
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
        assertTrue(Qudt.Units.N__PER__M2.matches(factors));
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
                assertTrue(Qudt.Units.KiloN__M.matches(factors), () -> "failed for " + factorUnits);
                successfulFor.add(new ArrayList<>(factorUnits));
            }
        } catch (AssertionFailedError e) {
            System.err.println("test succeeded for: ");
            successfulFor.forEach(System.err::println);
            System.err.println("test failed for:");
            System.err.println(factorUnits);
            throw e;
        }
        assertTrue(Qudt.Units.KiloN__M.matches(factors), () -> "failed for " + factorUnits);
        assertTrue(Qudt.Units.KiloJ.matches(factors), () -> "failed for " + factorUnits);
        Assertions.assertFalse(
                Qudt.Units.MilliOHM.matches(factors), () -> "failed for " + factorUnits);
        Assertions.assertFalse(
                Qudt.Units.MilliS.matches(factors), () -> "failed for " + factorUnits);
        factors = new Object[] {Qudt.Units.KiloGM, 1, Qudt.Units.K, -1, Qudt.Units.SEC, -3};
        Assertions.assertFalse(Qudt.Units.W__PER__K.matches(factors));
        Assertions.assertFalse(Qudt.Units.V__PER__K.matches(factors));
    }

    @Test
    public void testMilliJ() {
        Object[] factors =
                new Object[] {
                    Qudt.Units.KiloGM, 1, Qudt.Units.SEC, -2, Qudt.Units.M, 1, Qudt.Units.MilliM, 1
                };
        assertTrue(Qudt.Units.MilliN__M.matches(factors));
        Assertions.assertFalse(Qudt.Units.MilliH__PER__KiloOHM.matches(factors));
        assertTrue(Qudt.Units.MilliJ.matches(factors));
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
        assertTrue(Qudt.derivedUnitFromFactorUnits(simplified).contains(Qudt.Units.N__PER__M2));
        assertTrue(Qudt.derivedUnitFromFactorUnits(simplified).contains(Qudt.Units.PA));
    }

    @Test
    public void testScaleToBaseUnit() {
        Map.Entry<Unit, BigDecimal> base = Qudt.scaleToBaseUnit(Qudt.Units.KiloM);
        assertEquals(Qudt.Units.M, base.getKey());
        MatcherAssert.assertThat(base.getValue(), Matchers.comparesEqualTo(new BigDecimal("1000")));
    }
}
