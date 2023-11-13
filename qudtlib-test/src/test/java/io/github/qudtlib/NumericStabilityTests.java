package io.github.qudtlib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumericStabilityTests {

    @Test
    public void testStabilitiyOfSimpleFractions() {
        Assertions.assertEquals(
                "0.0010",
                Qudt.Prefixes.Milli.getMultiplier().toString(),
                "Numerically instable multiplier detected");
        /*
        Assertions.assertEquals(
                "0.0010",
                new BigDecimal("1.0E-3").toString(),
                "Numerically instable multiplier detected");
        Assertions.assertEquals(
                "0.001",
                BigDecimal.valueOf(1.0E-3).toString(),
                "Numerically instable multiplier detected");
        Assertions.assertEquals(
                "0.001",
                BigDecimal.valueOf(0.001).toString(),
                "Numerically instable multiplier detected");

        Assertions.assertEquals(
                "0.0010",
                new BigDecimal(0.001).toString(),
                "Numerically instable multiplier detected");
        Assertions.assertEquals(
                "0.0010",
                new BigDecimal(1.0E-3).toString(),
                "Numerically instable multiplier detected");

         */
    }
}
