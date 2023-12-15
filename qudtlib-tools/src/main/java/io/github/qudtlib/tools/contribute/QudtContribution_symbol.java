package io.github.qudtlib.tools.contribute;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.vocab.QUDT;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class QudtContribution_symbol {
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
                                            final String symbol = getSymbol(u).orElse(null);
                                            FactorUnits factorUnits = u.getFactorUnits();
                                            try {
                                                if (symbol != null) {
                                                    tool.addDerivedUnit(
                                                            factorUnits, ud -> ud.symbol(symbol));
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
                    }
                };

        for (int i = 0; i < 5; i++) {
            entityGenerator.unitOfWork(symbolAndUcumGenerator);
        }
        entityGenerator.writeRdf(s -> s.getPredicate().equals(QUDT.symbol));
    }

    private static Optional<String> getSymbol(Unit u) {
        List<FactorUnit> factorUnitList = u.getFactorUnits().getFactorUnits();
        if (!factorUnitList.isEmpty()) {
            // System.err.println(String.format("Cannot add symbol for %s: unit has no factor
            // units", u.getIri()));
            return new FactorUnits(factorUnitList).getSymbol();
        }
        if (u.isScaled()) {
            String baseUnitSymbol =
                    u.getScalingOf().map(base -> base.getSymbol().orElse(null)).orElse(null);
            if (baseUnitSymbol != null) {
                String prefixSymbol = u.getPrefix().map(p -> p.getSymbol()).orElse(null);
                if (prefixSymbol != null) {
                    return Optional.ofNullable(prefixSymbol + baseUnitSymbol);
                }
            }
        }
        return Optional.empty();
    }
}
