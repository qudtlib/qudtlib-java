package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qudtlib.model.FactorUnit;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FactorUnitTests {
    public static Stream<Arguments> testConversionMultiplier() {
        return Stream.of(
                Arguments.of(new FactorUnit(Qudt.Units.KiloM, 2), BigDecimal.valueOf(1000000)),
                Arguments.of(new FactorUnit(Qudt.Units.KiloM, -2), BigDecimal.valueOf(0.000001)));
    }

    @ParameterizedTest
    @MethodSource
    public void testConversionMultiplier(FactorUnit factorUnit, BigDecimal expectedResult) {
        MatcherAssert.assertThat(
                factorUnit.conversionMultiplier(), Matchers.comparesEqualTo(expectedResult));
    }

    @Test
    public void testDefinedAsOtherUnit() {
        assertTrue(Qudt.Units.L.isDefinedAsOtherUnit());
    }
}
