package io.github.qudtlib.tools;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.Tool;
import io.github.qudtlib.vocab.QUDT;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class QudtContribution231108 {
    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator(false);

        Consumer<Tool> symbolAndUcumGenerator =
                new Consumer<Tool>() {
                    @Override
                    public void accept(Tool tool) {
                        List<Unit> allUnits = new ArrayList<>(Qudt.allUnits());
                        allUnits.stream()
                                .sorted(Comparator.comparing(Unit::getIri))
                                .filter(u -> u.getSymbol().isEmpty())
                                .forEach(
                                        u -> {
                                            String symbol = null;
                                            FactorUnits factorUnits = null;
                                            if (u.isScaled()) {
                                                String baseUnitSymbol =
                                                        u.getScalingOf()
                                                                .map(
                                                                        base ->
                                                                                base.getSymbol()
                                                                                        .orElse(
                                                                                                null))
                                                                .orElse(null);
                                                if (baseUnitSymbol != null) {
                                                    String prefixSymbol =
                                                            u.getPrefix()
                                                                    .map(p -> p.getSymbol())
                                                                    .orElse(null);
                                                    if (prefixSymbol != null) {
                                                        symbol = prefixSymbol + baseUnitSymbol;
                                                        factorUnits = FactorUnits.ofUnit(u);
                                                    }
                                                }
                                            }
                                            if (symbol == null) {
                                                List<FactorUnit> factorUnitList =
                                                        u.getFactorUnits();
                                                if (factorUnitList.isEmpty()) {
                                                    // System.err.println(String.format("Cannot add
                                                    // symbol for %s: unit has no factor units",
                                                    // u.getIri()));
                                                    return;
                                                }
                                                factorUnits = new FactorUnits(factorUnitList);
                                                symbol = factorUnits.getSymbol().orElse(null);
                                            }
                                            final String finalSymbol = symbol;
                                            try {
                                                if (symbol != null) {
                                                    tool.addDerivedUnit(
                                                            factorUnits,
                                                            ud -> ud.symbol(finalSymbol));
                                                } else {
                                                    System.err.println(
                                                            String.format(
                                                                    "Cannot add symbol for %s: one of the constituent units has no symbol",
                                                                    u.getIri()));
                                                }
                                            } catch (Exception e) {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot add symbol for %s",
                                                                u.getIri()));
                                            }
                                        });
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
                                                    new FactorUnits(factorUnitList);
                                            try {
                                                String ucumCode =
                                                        factorUnits.getUcumCode().orElse(null);
                                                if (ucumCode != null) {
                                                    tool.addDerivedUnit(
                                                            factorUnits,
                                                            ud -> ud.ucumCode(ucumCode));
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
                    }
                };

        for (int i = 0; i < 10; i++) {
            entityGenerator.unitOfWork(symbolAndUcumGenerator);
        }
        entityGenerator.writeRdf(
                s ->
                        s.getPredicate().equals(QUDT.ucumCode)
                                || s.getPredicate().equals(QUDT.symbol));
    }
}
