package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContributeCorrectedDimensionVector {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        Map<Unit, String> newDimVectors = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .forEach(
                                    u -> {
                                        String dimVector = null;
                                        FactorUnits factorUnits = null;
                                        List<FactorUnit> factorUnitList = u.getFactorUnits();
                                        factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLabel(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        try {
                                            dimVector = factorUnits.getDimensionVectorIri();
                                            final String finalDimVector = dimVector;
                                            if (dimVector != null) {
                                                if (factorUnits
                                                                .getLocalname()
                                                                .equals(u.getIriLocalname())
                                                        && (u.getDimensionVectorIri().isEmpty()
                                                                || !u.getDimensionVectorIri()
                                                                        .get()
                                                                        .equals(dimVector))) {
                                                    if (u.getDimensionVectorIri().isPresent()) {
                                                        System.err.println(
                                                                String.format(
                                                                        "wrong dimension vector on %s %s will be replaced by %s",
                                                                        u.getIriAbbreviated(),
                                                                        u.getDimensionVectorIri()
                                                                                .get(),
                                                                        dimVector));
                                                    } else {
                                                        System.err.println(
                                                                String.format(
                                                                        "missing dimension vector on %s will be set to %s",
                                                                        u.getIriAbbreviated(),
                                                                        dimVector));
                                                    }
                                                    newDimVectors.put(u, dimVector);
                                                }
                                            } else {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot add symbol for %s: one of the constituent units has no symbol",
                                                                u.getIri()));
                                            }
                                        } catch (Exception e) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add symbol for %s: %s",
                                                            u.getIri(), e.getMessage()));
                                        }
                                    });
                });
        newDimVectors.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getIri()))
                .forEach(
                        e -> {
                            entityGenerator.unitOfWork(
                                    tool -> {
                                        tool.printFactorUnitTree(
                                                e.getKey(),
                                                u ->
                                                        String.format(
                                                                "%-30s\t%s",
                                                                (u.getUnit().getIriAbbreviated()
                                                                        + (u.getExponent() == 1
                                                                                ? ""
                                                                                : "^"
                                                                                        + u
                                                                                                .getExponent())),
                                                                u.getDimensionVectorIri().get()),
                                                new IndentedOutputStream(System.out, "# "));
                                        System.out.println(
                                                String.format(
                                                        "%s qudt:hasDimensionVector %s .",
                                                        e.getKey().getIriAbbreviated(),
                                                        QudtNamespaces.dimensionVector.abbreviate(
                                                                e.getValue())));
                                        System.out.println();
                                    });
                        });
    }
}
