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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CheckUcumCode {
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
                                    .filter(isLikelyDerivedUnit())
                                    .filter(
                                            u ->
                                                    !(u.getIriLocalname().equals("P")
                                                            || u.getIriLocalname()
                                                                    .toLowerCase()
                                                                    .contains("planck")
                                                            || u.getIriLocalname().equals("Quad")))
                                    .sorted(Comparator.comparing(u -> u.getIri()))
                                    .collect(Collectors.toList());
                    int correctUnits = -1;
                    Qudt.allUnits().stream()
                            .filter(isLikelyDerivedUnit().negate())
                            .forEach(globalData.correctUnits::add);
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
        System.out.println("DELETE { ?u qudt:ucumCode ?m } ");
        System.out.println("WHERE { ?u qudt:ucumCode ?m .");
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
        Optional<String> calculatedUcumCode =
                unit.getUcumCode().or(() -> unit.getFactorUnits().getUcumCode());
        if (calculatedUcumCode.isEmpty()) {
            return;
        } else {
            if (unit.getUcumCode().isPresent()) {
                String actualUcumCode = unit.getUcumCode().get();
                globalData.correctUnits.add(unit);
                boolean isRelevantDifference = !actualUcumCode.equals(calculatedUcumCode.get());
                if (isRelevantDifference) {
                    commentsForTTl.println(
                            format(
                                    "WRONG UCUMCODE  : %s - calculated from factors: %s, actual: %s\n",
                                    unit.getIriAbbreviated(),
                                    calculatedUcumCode.get().toString(),
                                    actualUcumCode.toString()));
                    commentsForTTl.println("Here is the triple you might want to use instead:");
                    printUcumCodeTriple(
                            commentsForTTl, commentsForTTl, unit, calculatedUcumCode.get());
                }
            } else {
                commentsForTTl.println(
                        format(
                                "MISSING UCUMCODE: %s - calculated from factors: %s\n",
                                unit.getIriAbbreviated(), calculatedUcumCode.get().toString()));
                printUcumCodeTriple(ttlPrintStream, commentsForTTl, unit, calculatedUcumCode.get());
                setUcumCode(unit, calculatedUcumCode.get());
                globalData.correctUnits.add(unit);
                globalData.wasMissing.add(unit);
            }
        }
    }

    private static Predicate<Unit> isLikelyDerivedUnit() {
        return u ->
                u.getIriLocalname().contains("-")
                        || (u.getFactorUnits().getLocalname().matches(".+\\d$")
                                && u.getFactorUnits().getFactorUnits().size() == 1
                                && u.getFactorUnits().getFactorUnits().get(0).getExponent() != 1);
    }

    private static void setUcumCode(Unit unit, String calculatedUcumCode) {
        try {
            Field ucumCodeField = Unit.class.getDeclaredField("ucumCode");
            ucumCodeField.setAccessible(true);
            ucumCodeField.set(unit, calculatedUcumCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void collectEffectsOfMissingUcumCode(Unit unit, GlobalData globalData) {
        unit.getFactorUnits()
                .streamAllFactorUnitsRecursively(fu -> fu.getUnit().getUcumCode().isEmpty())
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

    private static void printUcumCodeTriple(
            PrintStream printStream, PrintStream commentStream, Unit unit, String ucumCode) {
        String factorUnitTree = UnitTree.makeFactorUnitTreeShowingUcumCodes(unit);
        commentStream.print(factorUnitTree);
        printStream.format(
                "%s %s %s .\n\n",
                unit.getIriAbbreviated(),
                QudtNamespaces.qudt.abbreviate(QUDT.ucumCode.toString()),
                SimpleValueFactory.getInstance().createLiteral(ucumCode, QUDT.UCUMcs));
    }
}
