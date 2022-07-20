package org.example.qudlib;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.QuantityValue;
import io.github.qudtlib.model.Unit;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Simple demo of the QUDTLib usage.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QudtlibExample {
    public static void main(String[] args) {
        System.out.println(Qudt.getGreeting());
        System.out.println("---");
        System.out.println(
                "Converting 38.5° Celsius into Fahrenheit: "
                        + Qudt.convert(new BigDecimal("38.5"), Qudt.Units.DEG_C, Qudt.Units.DEG_F));
        System.out.println("---");
        System.out.println("finding unit for factors: m, kg, and s^-2:");
        Set<Unit> myUnits =
                Qudt.derivedUnit(
                        Qudt.Units.M, 1,
                        Qudt.Units.KiloGM, 1,
                        Qudt.Units.SEC, -2);
        for (Unit unit : myUnits) {
            System.out.println("unit : " + unit);
        }
        System.out.println("---");
        List<FactorUnit> myFactorUnits = Qudt.Units.N.getFactorUnits();
        System.out.println("finding factors of unit " + Qudt.Units.N);
        for (FactorUnit factorUnit : myFactorUnits) {
            System.out.println("factor unit:" + factorUnit);
        }
        System.out.println("---");
        System.out.print("Converting 1N into kN: ");
        QuantityValue quantityValue = new QuantityValue(new BigDecimal("1"), Qudt.Units.N);
        QuantityValue converted = Qudt.convert(quantityValue, Qudt.Units.KiloN);
        System.out.println(converted);
        System.out.println("---");
        System.out.print("Converting 1L into US Gallon: ");
        quantityValue = new QuantityValue(new BigDecimal("1"), Qudt.Units.L);
        converted = Qudt.convert(quantityValue, Qudt.Units.GAL_US);
        System.out.println(converted);
        System.out.println("---");
        System.out.println("Which units can we convert to from " + Qudt.Units.L + "?");
        Unit fromUnit = Qudt.Units.L;
        for (Unit unit : Qudt.allUnits()) {
            if (Qudt.isConvertible(fromUnit, unit)) {
                System.out.println("  " + unit + " (" + unit.getIri() + ")");
            }
        }
        System.out.println("---");
        System.out.println(
                "Which units are applicable for " + Qudt.QuantityKinds.PressureRatio + "?");
        for (String unitIri : Qudt.QuantityKinds.PressureRatio.getApplicableUnits()) {
            Unit unit = Qudt.unit(unitIri);
            System.out.println("  " + unit + " (" + unit.getIri() + ")");
        }
        System.out.println("---");
        System.out.println(
                "Instantiating unit by label 'Pint (UK)':" + Qudt.unitFromLabel("Pint (UK)"));
    }
}
