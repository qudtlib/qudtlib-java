package io.github.qudtlib.tools.contributions.archive;

import static io.github.qudtlib.model.Units.BYTE;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.math.BigDec;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.math.BigDecimal;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class CheckConversionMultipliersForScaledUnits {
    public static void main(String[] args) {
        Model statementsToAdd = new TreeModel();
        Model statementsToDelete = new TreeModel();
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    int totalCorrections = 0;
                    Qudt.allUnits().stream()
                            .filter(u -> u.getPrefix().isPresent())
                            .forEach(
                                    scaled -> {
                                        Optional<Unit> baseOpt = scaled.getScalingOf();
                                        if (baseOpt.isEmpty()) {
                                            System.out.println(
                                                    String.format(
                                                            "unit %s has prefix %s but no qudt:scalingOf!",
                                                            scaled.getIriAbbreviated(),
                                                            scaled.getPrefix()));
                                            return;
                                        }
                                        Unit base = baseOpt.get();
                                        if (base == BYTE) {
                                            // byte is weird, let's skip this
                                            return;
                                        }
                                        Optional<BigDecimal> scaledMultiplierOpt =
                                                scaled.getConversionMultiplier();
                                        Optional<BigDecimal> baseMultiplierOpt =
                                                base.getConversionMultiplier();
                                        if (scaledMultiplierOpt.isEmpty()) {
                                            System.out.println(
                                                    String.format(
                                                            "scaled unit %s has no multiplier",
                                                            scaled.getIriAbbreviated()));
                                        } else if (baseMultiplierOpt.isEmpty()) {
                                            System.out.println(
                                                    String.format(
                                                            "base unit %s of %s has no multiplier",
                                                            base.getIriAbbreviated(),
                                                            scaled.getIriAbbreviated()));
                                        } else {
                                            BigDecimal baseMultiplier = baseMultiplierOpt.get();
                                            BigDecimal scaledMultiplier = scaledMultiplierOpt.get();
                                            BigDecimal prefixMultiplier =
                                                    scaled.getPrefix().get().getMultiplier();
                                            FactorUnits fu = base.getFactorUnits();
                                            BigDecimal calculatedMultiplier =
                                                    prefixMultiplier.multiply(baseMultiplier);
                                            if (BigDec.isRelativeDifferenceGreaterThan(
                                                    scaledMultiplier,
                                                    calculatedMultiplier,
                                                    new BigDecimal("0.1"))) {
                                                System.out.println(
                                                        String.format(
                                                                "scaled unit %s has wrong multiplier",
                                                                scaled.getIriAbbreviated()));
                                                System.out.println(
                                                        String.format(
                                                                "base %s multiplier: %s",
                                                                base.getIriAbbreviated(),
                                                                baseMultiplier.toString()));
                                                System.out.println(
                                                        String.format(
                                                                "scaled %s multiplier: %s",
                                                                scaled.getIriAbbreviated(),
                                                                scaledMultiplier.toString()));
                                                System.out.println(
                                                        String.format(
                                                                "calculcated multiplier for %s (assuming base multiplier is correct): %s",
                                                                scaled.getIriAbbreviated(),
                                                                calculatedMultiplier));
                                            }
                                        }
                                    });
                });
    }
}
