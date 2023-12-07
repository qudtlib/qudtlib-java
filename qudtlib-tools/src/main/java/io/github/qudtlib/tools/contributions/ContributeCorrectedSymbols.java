package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.util.*;

public class ContributeCorrectedSymbols {
    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator(false);
        Map<Unit, String> newSymbols = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    List<Unit> allUnits = new ArrayList<>(Qudt.allUnits());
                    allUnits.stream()
                            .sorted(Comparator.comparing(Unit::getIri))
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
                                                                                    .orElse(null))
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
                                            List<FactorUnit> factorUnitList = u.getFactorUnits();
                                            if (factorUnitList.isEmpty()) {
                                                // System.err.println(String.format("Cannot add
                                                // symbol for %s: unit has no factor units",
                                                // u.getIri()));
                                                return;
                                            }
                                            factorUnits =
                                                    new FactorUnits(
                                                            FactorUnits.sortAccordingToUnitLabel(
                                                                    u.getIriLocalname(),
                                                                    factorUnitList));
                                            symbol = factorUnits.getSymbol().orElse(null);
                                        }
                                        final String finalSymbol = symbol;
                                        try {
                                            if (symbol != null) {
                                                if (factorUnits
                                                                .getLocalname()
                                                                .equals(u.getIriLocalname())
                                                        && (u.getSymbol().isEmpty()
                                                                || !u.getSymbol()
                                                                        .get()
                                                                        .equals(symbol))) {
                                                    System.err.println(
                                                            String.format(
                                                                    "wrong symbol on %s %s will be replaced by %s",
                                                                    u.getIriAbbreviated(),
                                                                    u.getSymbol().get(),
                                                                    symbol));
                                                    newSymbols.put(u, symbol);
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

        newSymbols.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getIri()))
                .forEach(
                        e ->
                                System.out.println(
                                        String.format(
                                                "%-30s \"%s\"",
                                                e.getKey().getIriAbbreviated(), e.getValue())));
    }
}
