package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.model.Units.MilliR_man;
import static io.github.qudtlib.model.Units.SV;

import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.math.BigDecimal;

public class ContributeRman {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    FactorUnits fu =
                            FactorUnits.builder()
                                    .scaleFactor(new BigDecimal("0.01"))
                                    .factor(SV)
                                    .build();
                    tool.addDerivedUnit(
                            fu,
                            def -> {
                                MilliR_man.getQuantityKinds().forEach(def::addQuantityKind);
                                def.symbol("rem")
                                        .ucumCode("REM")
                                        .clearLabels()
                                        .addLabel("roentgen equivalent man", "en");
                            },
                            meta ->
                                    meta.plainTextDescription(
                                                    "The roentgen equivalent man (rem)[1][2] is a CGS unit of equivalent dose, effective dose, and committed dose, which are dose measures used to estimate potential health effects of low levels of ionizing radiation on the human body.")
                                            .qudtInformativeReference(
                                                    "https://en.wikipedia.org/wiki/Roentgen_equivalent_man"),
                            "R_man");
                });
        entityGenerator.writeRdf();
    }
}
