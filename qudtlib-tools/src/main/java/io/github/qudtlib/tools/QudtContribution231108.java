package io.github.qudtlib.tools;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.entitygen.QudtEntityGenerator;

import static io.github.qudtlib.model.Units.*;

public class QudtContribution231108 {
    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        // check IFC measures here:
        // https://standards.buildingsmart.org/IFC/RELEASE/IFC4/ADD1/HTML/schema/ifcmeasureresource/content.htm
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #5: Regression test for rule Streckenlast zu Streckenlast_kN/m
                    // IFC: already mapped
                    FactorUnits fu = FactorUnits.ofFactorUnitSpec(Units.KiloN, 1, Units.M, -1);
                    tool.addDerivedUnit(
                            fu,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(Qudt.QuantityKinds.LinearForce),
                            metadata ->
                                    metadata.dcTermsDescription(
                                            "Scaling of Newton per meter by 1000"));
                });
        entityGenerator.unitOfWork(
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
                                                    .addLabel("Wärmekapazität je Fläche", "de"),
                                    metadata ->
                                            metadata.qudtInformativeReference(
                                                            "https://de.wikipedia.org/wiki/W%C3%A4rmekapazit%C3%A4t")
                                                    .qudtInformativeReference(
                                                            "https://help.autodesk.com/view/RVT/2022/ENU/?guid=GUID-F92A3954-CCF4-44D0-8066-B8DFC129E4D9"));
                    tool.addDerivedUnit(
                            jPerM2K,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(qk));
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #4: Regression test for rule Thermischer Gradientenkoeffizient für
                    // Feuchtigkeitskapazität zu Thermischer Gradientenkoeffizient für
                    // Feuchtigkeitskapazität_kg/kg°K
                    // IFC: unsure. TODO: ask
                    FactorUnits KgPerKgK =
                            FactorUnits.ofFactorUnitSpec(
                                    KiloGM, 1, KiloGM, -1, Units.K, -1);
                    tool.addDerivedUnit(
                            KgPerKgK,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(
                                                    Qudt.QuantityKinds
                                                            .ThermalExpansionCoefficient));
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #12: Regression test for rule Volumenstrom pro Leistung zu Volumenstrom
                    // pro Leistung_m³/(W*s)
                    // IFC: unsure. TODO: ask
                    FactorUnits M3PerWSEC =
                            FactorUnits.ofFactorUnitSpec(Units.M, 3, Units.W, -1, Units.SEC, -1);
                    String qkName = "VolumeFlowRatePerPower";
                    var qk =
                            tool.addQuantityKind(
                                    M3PerWSEC,
                                    qkName,
                                    quantityKindDef ->
                                            quantityKindDef
                                                    .addLabel("volume flow rate per power", "en")
                                                    .addLabel("Volumenstrom pro Leistung", "de"),
                                    metadata ->
                                            metadata.qudtInformativeReference(
                                                    "https://help.autodesk.com/view/RVTLT/2023/ENU/?guid=GUID-7E5F7EEA-5EBA-45A7-9CE5-6E9FF1B15053"));
                    tool.addDerivedUnit(
                            M3PerWSEC,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(qk));
                    tool.addDerivedUnit(FactorUnits.ofFactorUnitSpec(L, 1, SEC, -1, W, -1),
                                    unitDef -> unitDef.addSystemOfUnits(SystemsOfUnits.SI)
                                                    .addQuantityKind(qk));
                    tool.addDerivedUnit(FactorUnits.ofFactorUnitSpec(L, 1, SEC, -1, KiloW, -1),
                                    unitDef -> unitDef.addSystemOfUnits(SystemsOfUnits.SI)
                                                    .addQuantityKind(qk));
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #13: Regression test for rule Lineares Moment zu Lineares Moment_kNm/m
                    // TODO: quantitykind!?
                    FactorUnits kNmPerM =
                            FactorUnits.ofFactorUnitSpec(Qudt.Units.KiloN__M, 1, Qudt.Units.M, -1);
                    tool.checkQuantityKindExists(kNmPerM);
                    tool.addDerivedUnit(
                            kNmPerM, unitDef -> unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI));
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #14: Regression test for rule Winkelgeschwindigkeit zu
                    // Winkelgeschwindigkeit_rad*s /RPM (note: interpreted as rad/s OR RPM)
                    FactorUnits radS = FactorUnits.ofFactorUnitSpec(Qudt.Units.REV__PER__MIN, 1);
                    tool.checkUnitExists(radS);
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #18: Regression test for rule Federkonstante Drehfeder zu Federkonstante
                    // Drehfeder_kNm/Grad
                    FactorUnits kNmPerDeg =
                            FactorUnits.ofFactorUnitSpec(Units.KiloN__M, 1, Units.DEG, -1);
                    tool.checkUnitExists(kNmPerDeg);
                    QuantityKind quantityKind =
                            tool.addQuantityKind(
                                    kNmPerDeg,
                                    "SpringConstantTorsionSpring",
                                    qkDef -> qkDef.addLabel("Federkonstante Drehfeder", "de"));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.N__M, 1, Units.DEG, -1),
                            unitDef ->
                                    unitDef.addSystemOfUnits(SystemsOfUnits.SI)
                                            .addQuantityKind(quantityKind));
                    tool.addDerivedUnit(
                            kNmPerDeg,
                            unitDef ->
                                    unitDef.addSystemOfUnits(SystemsOfUnits.SI)
                                            .addQuantityKind(quantityKind));
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // Case #19: Regression test for rule Kostensatz - Energie zu Kostensatz -
                    // Energie_€/Wh
                    FactorUnits eurPerWHR =
                            FactorUnits.ofFactorUnitSpec(
                                    Units.EUR_Currency, 1, Units.W, -1, Units.HR, -1);
                    QuantityKind qk =
                            tool.addQuantityKind(
                                    eurPerWHR,
                                    "CostRateEnergy",
                                    quantityKindDef ->
                                            quantityKindDef
                                                    .addLabel("Energiekostensatz", "de")
                                                    .addLabel("cost rate energy", "en"),
                                    metadata ->
                                            metadata.qudtInformativeReference(
                                                    "https://help.autodesk.com/view/RVTLT/2023/ENU/?guid=GUID-7E5F7EEA-5EBA-45A7-9CE5-6E9FF1B15053"));

                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(
                                    Units.EUR_Currency, 1, Units.W, -1, Units.SEC, -1),
                            unitDef ->
                                    unitDef.addQuantityKind(qk)
                                            .addSystemOfUnits(SystemsOfUnits.SI));
                    tool.addDerivedUnit(
                            eurPerWHR,
                            unitDef ->
                                    unitDef.addQuantityKind(qk)
                                            .addSystemOfUnits(SystemsOfUnits.SI));
                });

        entityGenerator.unitOfWork(
                tool -> {
                    // Case #23: Regression test for rule Wölbwiderstand zu Wölbwiderstand_cm6
                    FactorUnits cm6 = FactorUnits.ofFactorUnitSpec(Units.CentiM, 6);
                    tool.checkUnitExists(cm6);
                    tool.checkQuantityKindExists(cm6);
                    tool.addDerivedUnit(
                            cm6,
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(QuantityKinds.WarpingConstant));
                });

        entityGenerator.unitOfWork(
                tool -> {
                    // Case #60: Regression test for rule Flächenlast zu Flächenlast_kN/m2
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.KiloN, 1, Units.M, -2),
                            unitDef ->
                                    unitDef.addSystemOfUnits(Qudt.SystemsOfUnits.SI)
                                            .addQuantityKind(Qudt.QuantityKinds.ForcePerArea)
                                            .build());
                });

        entityGenerator.unitOfWork(
                tool -> {
                    FactorUnits m2PerKW = FactorUnits.ofFactorUnitSpec(Units.M, 2, Units.KiloW, -1);
                    tool.checkUnitExists(m2PerKW);
                    tool.checkQuantityKindExists(m2PerKW);
                    // TODO: wait for input
                });

        entityGenerator.unitOfWork(
                tool -> {
                    // Case #24: Regression test for rule Fläche geteilt durch Heizlast zu Fläche
                    // geteilt durch Heizlast_m2/kW
                    FactorUnits WPerM2K =
                            FactorUnits.ofFactorUnitSpec(Units.W, 1, Units.M, -2, Units.K, -1);
                    tool.checkUnitExists(WPerM2K);
                    tool.checkQuantityKindExists(WPerM2K);
                    tool.searchQuantityKinds("heating");
                    QuantityKind areaPerHeatingLoad =
                            tool.addQuantityKind(
                                    WPerM2K,
                                    "AreaPerHeatingLoad",
                                    qkDef ->
                                            qkDef.addLabel("area per heating load", "en")
                                                    .addLabel("Fläche pro Heizlast", "de")
                                                    .dimensionVectorIri(
                                                            WPerM2K.getDimensionVectorIri()));
                    tool.addDerivedUnit(
                            WPerM2K,
                            unitDef ->
                                    unitDef.addSystemOfUnits(SystemsOfUnits.SI)
                                            .addQuantityKind(areaPerHeatingLoad));
                });
        entityGenerator.unitOfWork(
                // Case #25: Regression test for rule Wärmeleitfähigkeit zu Wärmeleitfähigkeit_W/m*K
                tool -> {
                    FactorUnits WPerMK =
                            FactorUnits.ofFactorUnitSpec(Units.W, 1, Units.M, -1, Units.K, -1);
                    tool.checkUnitExists(WPerMK);
                    tool.checkQuantityKindExists(WPerMK);
                });
        entityGenerator.unitOfWork(
                tool -> {
                    // apparently, we need °C as well as K. TODO: add QKs
                    tool.checkQuantityKindExists(
                            FactorUnits.ofFactorUnitSpec(Units.J, 1, Units.GM, -1, Units.K, -1));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(
                                    Units.J, 1, KiloGM, -1, Units.DEG_C, -1),
                            unitDef -> unitDef.addSystemOfUnits(SystemsOfUnits.SI));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.J, 1, Units.GM, -1, Units.DEG_C, -1),
                            unitDef -> unitDef.addSystemOfUnits(SystemsOfUnits.SI));
                    tool.addDerivedUnit(
                            FactorUnits.ofFactorUnitSpec(Units.J, 1, Units.GM, -1, Units.K, -1),
                            unitDef -> unitDef.addSystemOfUnits(SystemsOfUnits.SI));
                    tool.checkUnitExists(
                            FactorUnits.ofFactorUnitSpec(Units.J, 1, Units.GM, -1, Units.DEG_C, -1),
                            DerivedUnitSearchMode.ALL);
                    tool.listUnitsWithSameDimensions(Units.J__PER__KiloGM__K);
                });

        entityGenerator.unitOfWork(
                        tool -> {
                            tool.checkQuantityKindExists(FactorUnits.ofFactorUnitSpec(LM,1, W,-1));
                        }
        );



        entityGenerator.writeRdf();
    }
}
