package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.model.Units.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class FindFactorsToFix {
    public static void main(String[] args) {
        QudtEntityGenerator gen = new QudtEntityGenerator();
        gen.unitOfWork(
                tool -> {
                    List<Unit> unitsWithNonBaseFactors =
                            Qudt.allUnits().stream()
                                    .filter(u -> !u.isDeprecated())
                                    .filter(u -> u.getConversionMultiplier().isPresent())
                                    .filter(
                                            u ->
                                                    u.getConversionMultiplier()
                                                                    .get()
                                                                    .compareTo(BigDecimal.ONE)
                                                            != 0)
                                    .sorted(Comparator.comparing(Unit::getIri))
                                    .sorted(
                                            Comparator.comparing(
                                                    u ->
                                                            u.getDimensionVectorIri()
                                                                    .orElse("no dim vector")))
                                    .filter(
                                            u ->
                                                    u
                                                            .getFactorUnits()
                                                            .normalize()
                                                            .getFactorUnits()
                                                            .stream()
                                                            .anyMatch(
                                                                    fu ->
                                                                            !isKnownNonBaseUnit(fu)
                                                                                    && fu.getUnit()
                                                                                                    .getConversionMultiplier()
                                                                                                    .orElse(
                                                                                                            BigDecimal
                                                                                                                    .ZERO)
                                                                                                    .compareTo(
                                                                                                            BigDecimal
                                                                                                                    .ONE)
                                                                                            != 0))
                                    .collect(Collectors.toList());
                    Map<Unit, Integer> nonbaseOccurrences = new HashMap<>();
                    for (Unit unitWithNonBaseFactor : unitsWithNonBaseFactors) {
                        System.err.println(
                                String.format(
                                        "unit with non-base factor (factor whose conversionMultiplier is not 1): %s",
                                        unitWithNonBaseFactor.getIriAbbreviated()));
                        System.err.println(
                                String.format(
                                        "normalized factor units: %s",
                                        unitWithNonBaseFactor.getFactorUnits().normalize()));
                        List<Unit> nonbaseFactors =
                                unitWithNonBaseFactor
                                        .getFactorUnits()
                                        .normalize()
                                        .getFactorUnits()
                                        .stream()
                                        .filter(
                                                fu ->
                                                        !isKnownNonBaseUnit(fu)
                                                                && fu.getUnit()
                                                                                .getConversionMultiplier()
                                                                                .orElse(
                                                                                        BigDecimal
                                                                                                .ZERO)
                                                                                .compareTo(
                                                                                        BigDecimal
                                                                                                .ONE)
                                                                        != 0)
                                        .map(FactorUnit::getUnit)
                                        .collect(Collectors.toList());
                        String toPrint =
                                nonbaseFactors.stream()
                                        .map(
                                                fu ->
                                                        fu.getIriAbbreviated()
                                                                + ": "
                                                                + fu.getConversionMultiplier()
                                                                        .map(Objects::toString)
                                                                        .orElse("[no multiplier]"))
                                        .collect(Collectors.joining(", "));
                        System.err.println(String.format("non-base factor units: %s", toPrint));
                        nonbaseFactors.stream()
                                .forEach(
                                        u ->
                                                nonbaseOccurrences.compute(
                                                        u,
                                                        (unit, cnt) -> cnt == null ? 1 : cnt + 1));
                        try {
                            tool.printFactorUnitTree(unitWithNonBaseFactor);
                        } catch (Exception e) {
                            System.err.println(
                                    "[cannot print factor unit tree]: " + e.getMessage());
                        }
                    }
                    System.err.println("nonbase factors to fix: " + nonbaseOccurrences.size());
                    System.err.println(
                            nonbaseOccurrences.entrySet().stream()
                                    .sorted(Comparator.comparing(e -> e.getValue()))
                                    .map(
                                            e ->
                                                    String.format(
                                                            "%s: %d occurrences",
                                                            e.getKey().getIriAbbreviated(),
                                                            e.getValue()))
                                    .collect(Collectors.joining("\n")));
                });
    }

    private static List<Unit> KNOWN_NONBASE_UNITS = List.of(GM, BIT, BYTE);

    private static boolean isKnownNonBaseUnit(FactorUnit fu) {
        return KNOWN_NONBASE_UNITS.contains(fu.getUnit()) || fu.getUnit().isCurrencyUnit();
    }
}
