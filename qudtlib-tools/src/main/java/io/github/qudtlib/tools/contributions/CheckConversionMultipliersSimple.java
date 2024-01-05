package io.github.qudtlib.tools.contributions;

import static java.lang.String.format;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.math.BigDec;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CheckConversionMultipliersSimple {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    List<Unit> unitsToCheck =
                            Qudt.allUnits().stream()
                                    .filter(u -> !u.isCurrencyUnit() && !u.isDeprecated())
                                    .sorted(Comparator.comparing(u -> u.getIri()))
                                    .collect(Collectors.toList());
                    for (Unit unit : unitsToCheck) {
                        BigDecimal calculatedMultiplier =
                                unit.getFactorUnits().getConversionMultiplier();
                        if (unit.getConversionMultiplier().isPresent()) {
                            BigDecimal actualMultiplier = unit.getConversionMultiplier().get();
                            boolean isRelevantDifference =
                                    BigDec.isRelativeDifferenceGreaterThan(
                                            calculatedMultiplier,
                                            actualMultiplier,
                                            new BigDecimal("0.0001"));
                            if (isRelevantDifference) {
                                System.out.println(
                                        format(
                                                "WRONG MULTIPLIER  : %s - calculated from factors: %s, actual: %s",
                                                unit.getIriAbbreviated(),
                                                calculatedMultiplier.toString(),
                                                actualMultiplier.toString()));
                            }
                        } else {
                            if (calculatedMultiplier.compareTo(BigDecimal.ONE) != 0) {
                                System.out.println(
                                        format(
                                                "MISSING MULTIPLIER: %s - calculated from factors: %s",
                                                unit.getIriAbbreviated(),
                                                calculatedMultiplier.toString()));
                            }
                        }
                    }
                });
    }
}
