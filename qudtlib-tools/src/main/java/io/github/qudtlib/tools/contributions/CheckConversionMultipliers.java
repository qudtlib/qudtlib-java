package io.github.qudtlib.tools.contributions;

import static java.util.stream.Collectors.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.DimensionVector;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckConversionMultipliers {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    Map<String, Set<Unit>> baseUnits =
                            Qudt.allUnits().stream()
                                    .filter(
                                            u ->
                                                    u.getConversionMultiplier()
                                                            .map(
                                                                    m ->
                                                                            m.compareTo(
                                                                                            BigDecimal
                                                                                                    .ONE)
                                                                                    == 0)
                                                            .orElse(false))
                                    .collect(
                                            groupingBy(
                                                    u -> u.getDimensionVectorIri().orElse("[none]"),
                                                    toSet()));
                    for (String dimVector : baseUnits.keySet()) {
                        System.out.println(
                                String.format(
                                        "Units with Dimension Vector %s and conversionMultiplier 1.0:",
                                        QudtNamespaces.dimensionVector.abbreviate(dimVector)));
                        if (DimensionVector.of(dimVector)
                                .map(DimensionVector::isDimensionless)
                                .orElse(false)) {
                            System.out.println(
                                    "skipping dimensionless units (too many for this check)");
                            continue;
                        }
                        List<Unit> bases = new ArrayList<>(baseUnits.get(dimVector));
                        if (bases.size() > 1) {
                            for (int i = 0; i < bases.size(); i++) {
                                for (int j = 0; j < bases.size(); j++) {
                                    Unit from = bases.get(i);
                                    Unit to = bases.get(j);
                                    try {
                                        BigDecimal factor =
                                                new FactorUnits(from.getFactorUnits())
                                                        .conversionFactor(to);
                                        if (BigDecimal.ONE.compareTo(factor) != 0) {
                                            System.out.println(
                                                    String.format(
                                                            " 1 %s should be 1 %s but based on the factor units, it is 1 %s = %s %s",
                                                            from.getIriAbbreviated(),
                                                            to.getIriAbbreviated(),
                                                            from.getIriAbbreviated(),
                                                            factor.toString(),
                                                            to.getIriAbbreviated()));
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e.getMessage()); // ignore for now
                                    }
                                }
                            }
                        }
                        System.out.println(
                                baseUnits.get(dimVector).stream()
                                        .map(u -> u.getIriAbbreviated())
                                        .collect(joining("\n\t", "\n\t", "\n")));
                    }
                });
    }
}
