package io.github.qudtlib;

import io.github.qudtlib.model.SystemOfUnits;
import io.github.qudtlib.model.Unit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemOfUnitsTests {

    @Test
    public void testMetreInSI() {
        Unit metre = Qudt.Units.M;
        Assertions.assertFalse(metre.getUnitOfSystems().isEmpty());
        Assertions.assertTrue(
                metre.getUnitOfSystems().stream().anyMatch(s -> s.getAbbreviation().equals("SI")));
    }

    @Test
    public void testSIUnits() {
        SystemOfUnits si = Qudt.SystemsOfUnits.SI;
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.M));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.KiloGM));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.A));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.SEC));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.CD));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.MOL));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.UNITLESS));
        Assertions.assertTrue(si.getBaseUnits().contains(Qudt.Units.K));
        Assertions.assertEquals(8, si.getBaseUnits().size());
    }
}
