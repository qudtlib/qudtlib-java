package some.pkg;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.Units;
import io.github.qudtlib.tools.entitygen.QudtEntityGenerator;

public class QudtContributionExample {
    public static void main(String[] args) {
        QudtEntityGenerator contributionTool = new QudtEntityGenerator();

        // add entities
        contributionTool.unitOfWork(
                tool -> {
                    // Case #3: Regression test for rule Wärmekapazität pro Fläche zu Wärmekapazität
                    // pro Fläche_J/m²K
                    // IFC: no IFC MEASURE (userdefined)
                    FactorUnits jPerM2K =
                            FactorUnits.ofFactorUnitSpec(Units.J, 1, Units.M, -2, Units.K, -1);
                    QuantityKind qk =
                            tool.addQuantityKind(
                                    jPerM2K,
                                    "HeatCapacityPerArea",
                                    unitDef ->
                                            unitDef.addLabel("heat capacity per area", "en")
                                                    .addLabel("Wärmekapazität je Fläche", "de"));
                    tool.addDerivedUnit(
                            jPerM2K,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(qk));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.KiloJ, 1, Units.M, -2, Units.K, -1),
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(qk));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.MegaJ, 1, Units.M, -2, Units.K, -1),
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(qk));
                });

        // write RDF
        contributionTool.writeRdf();
    }
}
