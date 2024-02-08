package io.github.qudtlib.tools.contributions;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.tree.UnitTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckSymbols {
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
                                    .filter(u -> !u.isGenerated())
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
        System.out.println("DELETE { ?u qudt:symbol ?m } ");
        System.out.println("WHERE { ?u qudt:symbol ?m .");
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
        Optional<String> calculatedSymbol =
                unit.getSymbol().or(() -> unit.getFactorUnits().getSymbol());
        if (calculatedSymbol.isEmpty()) {
            return;
        } else {
            if (unit.getSymbol().isPresent()) {
                String actualSymbol = unit.getSymbol().get();
                globalData.correctUnits.add(unit);
                boolean isRelevantDifference = !actualSymbol.equals(calculatedSymbol.get());
                if (isRelevantDifference) {
                    commentsForTTl.println(
                            format(
                                    "WRONG SYMBOL?  : %s - calculated from factors: %s, actual: %s\n",
                                    unit.getIriAbbreviated(),
                                    calculatedSymbol.get().toString(),
                                    actualSymbol.toString()));
                    commentsForTTl.println("Here is the triple you might want to use instead:");
                    printSymbolTriple(commentsForTTl, commentsForTTl, unit, calculatedSymbol.get());
                }
            } else {
                commentsForTTl.println(
                        format(
                                "MISSING SYMBOL: %s - calculated from factors: %s\n",
                                unit.getIriAbbreviated(), calculatedSymbol.get().toString()));
                printSymbolTriple(ttlPrintStream, commentsForTTl, unit, calculatedSymbol.get());
                setSymbol(unit, calculatedSymbol.get());
                globalData.correctUnits.add(unit);
                globalData.wasMissing.add(unit);
            }
        }
    }

    private static void setSymbol(Unit unit, String calculatedSymbol) {
        try {
            Field symbolField = Unit.class.getDeclaredField("symbol");
            symbolField.setAccessible(true);
            symbolField.set(unit, calculatedSymbol);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void collectEffectsOfMissingSymbol(Unit unit, GlobalData globalData) {
        unit.getFactorUnits()
                .streamAllFactorUnitsRecursively(fu -> fu.getUnit().getSymbol().isEmpty())
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

    private static void printSymbolTriple(
            PrintStream printStream, PrintStream commentStream, Unit unit, String symbol) {
        String factorUnitTree = UnitTree.makeFactorUnitTreeShowingSymbols(unit);
        commentStream.print(factorUnitTree);
        printStream.format(
                "%s %s \"%s\" .\n\n",
                unit.getIriAbbreviated(),
                QudtNamespaces.qudt.abbreviate(QUDT.symbol.toString()),
                symbol);
    }
}
