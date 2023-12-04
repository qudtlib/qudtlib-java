package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FactorUnitsTests {

    @ParameterizedTest
    @MethodSource("testNormalizeFactorUnits")
    public void testNormalizeFactorUnits(
            FactorUnits factorUnitsToNormalize, FactorUnits expectedResult) {
        assertEquals(factorUnitsToNormalize.normalize(), expectedResult);
    }

    public static Stream<Arguments> testNormalizeFactorUnits() {
        return Stream.of(
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.M, 2),
                        FactorUnits.ofFactorUnitSpec(Units.M, 2)),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1).getFactorUnits(),
                                BigDecimal.valueOf(1E3))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.KiloGM, 2),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 2).getFactorUnits(),
                                BigDecimal.valueOf(1E6))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1, Units.M, -3),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E3))),
                Arguments.of(
                        FactorUnits.ofFactorUnitSpec(Units.KiloGM, 1, Units.M3, -1),
                        new FactorUnits(
                                FactorUnits.ofFactorUnitSpec(Units.GM, 1, Units.M, -3)
                                        .getFactorUnits(),
                                BigDecimal.valueOf(1E3))));
    }
}
