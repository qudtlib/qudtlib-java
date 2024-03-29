package io.github.qudtlib.tools.contributions;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.math.BigDec;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.FormattingHelper;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.tree.UnitTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckConversionMultipliers {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        GlobalData globalData = new GlobalData();
        ByteArrayOutputStream ttlOut = new ByteArrayOutputStream();
        PrintStream ttlPrintStream = new PrintStream(ttlOut);
        entityGenerator.unitOfWork(
                tool -> {
                    List<Unit> unitsToCheck =
                            Qudt.allUnits().stream()
                                    .filter(u -> !u.isCurrencyUnit() && !u.isDeprecated())
                                    .sorted(Comparator.comparing(u -> u.getIri()))
                                    .collect(Collectors.toList());
                    int correctUnits = -1;
                    while (correctUnits < globalData.correctUnits.size()) {
                        correctUnits = globalData.correctUnits.size();
                        unitsToCheck.stream()
                                .filter(u -> !globalData.correctUnits.contains(u))
                                .forEach(unit -> checkUnit(unit, globalData, ttlPrintStream));
                        System.out.println(
                                String.format(
                                        "was incorrect: \n%s\n",
                                        globalData.wasIncorrect.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining("\n"))));
                        System.out.println(
                                String.format(
                                        "was missing: \n%s\n",
                                        globalData.wasMissing.stream()
                                                .map(Unit::getIriAbbreviated)
                                                .collect(Collectors.joining("\n"))));
                    }
                });
        globalData.missingData.entrySet().stream()
                .sorted(
                        Comparator.comparing((Map.Entry<Unit, Set<Unit>> e) -> e.getValue().size())
                                .reversed())
                .forEach(
                        missing -> {
                            System.out.println(
                                    format(
                                            "MISSING MULTIPLIER: %s - therefore cannot calculate multiplier of %s",
                                            missing.getKey().getIriAbbreviated(),
                                            missing.getValue().stream()
                                                    .map(Unit::getIriAbbreviated)
                                                    .collect(Collectors.joining(", "))));
                        });
        printStatements(ttlOut);
        printDeleteQuery(globalData);
    }

    private static void printStatements(ByteArrayOutputStream ttlOut) {
        System.out.println("STATEMENTS TO ADD:\n\n");
        System.out.println(ttlOut.toString());
    }

    private static void printDeleteQuery(GlobalData globalData) {
        System.out.println("\n\n\nSTATEMENTS TO DELETE\n\n");
        System.out.println("PREFIX qudt: <http://qudt.org/schema/qudt/>");
        System.out.println("PREFIX unit: <http://qudt.org/vocab/unit/>");
        System.out.println("DELETE { ?u qudt:conversionMultiplier ?m } ");
        System.out.println("WHERE { ?u qudt:conversionMultiplier ?m .");
        System.out.println("VALUES  ?u {");
        System.out.println(
                Stream.concat(globalData.wasMissing.stream(), globalData.wasIncorrect.stream())
                        .map(Unit::getIriAbbreviated)
                        .collect(joining("\n\t")));
        System.out.println("}}");
    }

    private static void checkUnit(Unit unit, GlobalData globalData, PrintStream ttlPrintStream) {
        PrintStream commentsForTTl = new IndentedOutputStream(ttlPrintStream, "  # ").printStream();
        if (!globalData.trustCalculationForUnit(unit)) {
            return;
        }
        Optional<BigDecimal> calculatedMultiplier =
                unit.getFactorUnits().getConversionMultiplierOpt();
        if (calculatedMultiplier.isEmpty()) {
            return;
        } else {
            if (unit.getConversionMultiplier().isPresent()) {
                BigDecimal actualMultiplier = unit.getConversionMultiplier().get();
                boolean isRelevantDifference =
                        BigDec.isRelativeDifferenceGreaterThan(
                                calculatedMultiplier.get(),
                                actualMultiplier,
                                new BigDecimal(globalData.relativeDifferenceThreshold));
                if (isRelevantDifference) {
                    commentsForTTl.println(
                            format(
                                    "WRONG MULTIPLIER  : %s - calculated from factors: %s, actual: %s\n",
                                    unit.getIriAbbreviated(),
                                    calculatedMultiplier.get().toString(),
                                    actualMultiplier.toString()));
                    printConversionMultiplierTriple(
                            ttlPrintStream, commentsForTTl, unit, calculatedMultiplier.get());
                    setMultiplier(unit, calculatedMultiplier);
                    globalData.correctUnits.add(unit);
                    globalData.wasIncorrect.add(unit);
                } else {
                    globalData.correctUnits.add(unit);
                }
            } else {
                commentsForTTl.println(
                        format(
                                "MISSING MULTIPLIER: %s - calculated from factors: %s\n",
                                unit.getIriAbbreviated(), calculatedMultiplier.get().toString()));
                printConversionMultiplierTriple(
                        ttlPrintStream, commentsForTTl, unit, calculatedMultiplier.get());
                setMultiplier(unit, calculatedMultiplier);
                globalData.correctUnits.add(unit);
                globalData.wasMissing.add(unit);
            }
        }
    }

    private static void setMultiplier(Unit unit, Optional<BigDecimal> calculatedMultiplier) {
        try {
            Field conversionMultiplierField = Unit.class.getDeclaredField("conversionMultiplier");
            conversionMultiplierField.setAccessible(true);
            conversionMultiplierField.set(unit, calculatedMultiplier.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void collectEffectsOfMissingMultiplier(Unit unit, GlobalData globalData) {
        unit.getFactorUnits()
                .streamAllFactorUnitsRecursively(
                        fu -> fu.getUnit().getConversionMultiplier().isEmpty())
                .map(FactorUnit::getUnit)
                .forEach(
                        causeUnit ->
                                globalData.missingData.compute(
                                        causeUnit,
                                        (u, missing) -> {
                                            if (missing == null) {
                                                missing = new HashSet<>();
                                            }
                                            missing.add(unit);
                                            return missing;
                                        }));
    }

    private static void printConversionMultiplierTriple(
            PrintStream printStream,
            PrintStream commentStream,
            Unit nonBaseUnit,
            BigDecimal conversionFactorToBase) {
        String factorUnitTree =
                UnitTree.makeFactorUnitTreeShowingConversionMultipliers(nonBaseUnit);
        commentStream.print(factorUnitTree);
        printStream.format(
                "%s %s %s .\n\n",
                nonBaseUnit.getIriAbbreviated(),
                QudtNamespaces.qudt.abbreviate(QUDT.conversionMultiplier.toString()),
                FormattingHelper.format(conversionFactorToBase));
    }
}
