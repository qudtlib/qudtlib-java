package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.vocab.QUDT;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContributeMissingUcumCode {

    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator(false);

        entityGenerator.unitOfWork(
                tool -> {
                    List<Unit> allUnits = new ArrayList<>(Qudt.allUnits());
                    allUnits.stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .filter(u -> u.getUcumCode().isEmpty())
                            .forEach(
                                    u -> {
                                        List<FactorUnit> factorUnitList = u.getFactorUnits();
                                        if (factorUnitList.isEmpty()) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add ucumCode for %s: unit has no factor units",
                                                            u.getIri()));
                                            return;
                                        }
                                        FactorUnits factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLabel(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        try {
                                            String ucumCode =
                                                    factorUnits.getUcumCode().orElse(null);
                                            if (ucumCode != null) {
                                                tool.addDerivedUnit(
                                                        factorUnits, ud -> ud.ucumCode(ucumCode));
                                            } else {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot add ucumCode for %s: one of the constituent units has no ucumCode",
                                                                u.getIri()));
                                            }
                                        } catch (Exception e) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add ucum code for %s",
                                                            u.getIri()));
                                        }
                                    });
                });
        entityGenerator.writeRdf(s -> s.getPredicate().equals(QUDT.ucumCode));
    }
}
