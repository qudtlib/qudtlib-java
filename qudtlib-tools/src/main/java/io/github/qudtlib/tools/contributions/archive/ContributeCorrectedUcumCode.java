package io.github.qudtlib.tools.contributions.archive;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.util.*;

public class ContributeCorrectedUcumCode {

    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator(false);
        Map<Unit, String> newSymbols = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    List<Unit> allUnits = new ArrayList<>(Qudt.allUnits());
                    allUnits.stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .filter(u -> u.getUcumCode().isEmpty())
                            .forEach(
                                    u -> {
                                        List<FactorUnit> factorUnitList =
                                                u.getFactorUnits().getFactorUnits();
                                        if (factorUnitList.isEmpty()) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add ucumCode for %s: unit has no factor units",
                                                            u.getIri()));
                                            return;
                                        }
                                        FactorUnits factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLocalname(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        try {
                                            String ucumCode =
                                                    factorUnits.getUcumCode().orElse(null);
                                            if (ucumCode != null) {
                                                if (factorUnits
                                                                .getLocalname()
                                                                .equals(u.getIriLocalname())
                                                        && (u.getUcumCode().isEmpty()
                                                                || !u.getUcumCode()
                                                                        .get()
                                                                        .equals(ucumCode))) {
                                                    if (u.getUcumCode().isEmpty()) {
                                                        System.err.println(
                                                                String.format(
                                                                        "missing ucum code %s will be set to %s",
                                                                        u.getIriAbbreviated(),
                                                                        ucumCode));
                                                    } else {
                                                        System.err.println(
                                                                String.format(
                                                                        "wrong ucum code on %s %s will be replaced by %s",
                                                                        u.getIriAbbreviated(),
                                                                        u.getUcumCode().get(),
                                                                        ucumCode));
                                                    }
                                                    newSymbols.put(u, ucumCode);
                                                } else {
                                                    System.err.println(
                                                            "Not adding ucum code for "
                                                                    + u.getIriAbbreviated());
                                                }
                                            } else {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot add ucumCode for %s: one of the constituent units has no ucumCode",
                                                                u.getIri()));
                                            }
                                        } catch (Exception e) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add ucum code for %s: %s",
                                                            u.getIri(), e.getMessage()));
                                        }
                                    });
                });
        newSymbols.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getIri()))
                .forEach(
                        e ->
                                System.out.println(
                                        String.format(
                                                "%s qudt:ucumCode \"%s\"^^qudt:UCUMcs .",
                                                e.getKey().getIriAbbreviated(), e.getValue())));
    }
}
