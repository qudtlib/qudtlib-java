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
import java.util.stream.Collectors;

public class QudtContribution_ucumCode {
    public static void main(String[] args) throws Exception {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator(false);

        Consumer<Tool> symbolAndUcumGenerator =
                new Consumer<Tool>() {
                    @Override
                    public void accept(Tool tool) {
                        List<Unit> allUnits = new ArrayList<>(Qudt.allUnits());
                        allUnits.stream()
                                .sorted(Comparator.comparing(Unit::getIri))
                                .filter(u -> u.getUcumCode().isEmpty())
                                .forEach(
                                        u -> {
                                            final String ucumCode = getUcumCode(u).orElse(null);
                                            FactorUnits factorUnits =
                                                    u.hasFactorUnits()
                                                            ? new FactorUnits(u.getFactorUnits())
                                                            : FactorUnits.ofUnit(u);
                                            try {
                                                if (ucumCode != null) {
                                                    tool.addDerivedUnit(
                                                            factorUnits,
                                                            ud -> ud.ucumCode(ucumCode));
                                                } else {
                                                    System.err.println(
                                                            String.format(
                                                                    "Cannot add ucum code for %s: one of the constituent units has no ucum code",
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

        for (int i = 0; i < 5; i++) {
            entityGenerator.unitOfWork(symbolAndUcumGenerator);
        }
        entityGenerator.writeRdf(s -> s.getPredicate().equals(QUDT.ucumCode));
    }

    private static Optional<String> getUcumCode(Unit u) {
        List<FactorUnit> factorUnitList = u.getFactorUnits();
        if (!factorUnitList.isEmpty()) {
            // System.err.println(String.format("Cannot add symbol for %s: unit has no factor
            // units", u.getIri()));
            return new FactorUnits(
                            factorUnitList.stream()
                                    .map(
                                            fu ->
                                                    new FactorUnit(
                                                            Qudt.unitRequired(
                                                                    fu.getUnit().getIri()),
                                                            fu.getExponent()))
                                    .collect(Collectors.toList()))
                    .getUcumCode();
        }
        if (u.isScaled()) {
            String baseUnitSymbol =
                    u.getScalingOf()
                            .map(base -> Qudt.unitRequired(base.getIri()))
                            .map(base -> base.getUcumCode().orElse(null))
                            .orElse(null);
            if (baseUnitSymbol != null) {
                String prefixSymbol =
                        u.getPrefix().map(p -> p.getUcumCode().orElse(null)).orElse(null);
                if (prefixSymbol != null) {
                    return Optional.ofNullable(prefixSymbol + baseUnitSymbol);
                }
            }
        }
        return Optional.empty();
    }
}
