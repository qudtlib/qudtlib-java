package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.math.BigDec.isRelativeDifferenceGreaterThan;
import static io.github.qudtlib.model.Units.GM;
import static java.util.stream.Collectors.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.DimensionVector;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.Tool;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.SelectionHelper;
import io.github.qudtlib.tools.contribute.support.tree.Node;
import io.github.qudtlib.tools.contribute.support.tree.QuantityKindTree;
import io.github.qudtlib.tools.contribute.support.tree.UnitTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class CheckConversionMultipliers {
    private static final String NO_DIM_VECTOR = "[no dimension vector]";

    private static class UnitConversionFactor {
        private Unit unit;
        private BigDecimal factor;

        public UnitConversionFactor(Unit unit, BigDecimal factor) {
            this.unit = unit;
            this.factor = factor;
        }

        public boolean conversionFailed() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UnitConversionFactor)) return false;
            UnitConversionFactor that = (UnitConversionFactor) o;
            return Objects.equals(unit, that.unit) && Objects.equals(factor, that.factor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, factor);
        }
    }

    private static class UnitConversionFailed extends UnitConversionFactor {
        private String errormessage;

        public UnitConversionFailed(Unit unit, BigDecimal factor, String errormessage) {
            super(unit, factor);
            this.errormessage = errormessage;
        }

        public boolean conversionFailed() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UnitConversionFailed)) return false;
            if (!super.equals(o)) return false;
            UnitConversionFailed that = (UnitConversionFailed) o;
            return Objects.equals(errormessage, that.errormessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), errormessage);
        }
    }

    public static void main(String[] args) {
        Model statementsToAdd = new TreeModel();
        Model statementsToDelete = new TreeModel();
        ByteArrayOutputStream ttlOut = new ByteArrayOutputStream();
        PrintStream ttlPrintStream = new PrintStream(ttlOut);
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    List<String> dimVectors =
                            findAllDimensionVectors().stream().sorted().collect(toList());
                    for (String dimVector : dimVectors) {
                        analyzeUnitsWithDimVector(
                                dimVector, ttlPrintStream, statementsToDelete, tool);
                    }
                    System.out.format("statements to add: %d\n", statementsToAdd.size());
                    System.out.format("statements to delete: %d\n", statementsToDelete.size());
                    System.out.println("STATEMENTS TO ADD");
                    tool.writeOut(statementsToAdd, System.out, s -> true);
                    try {
                        ttlPrintStream.close();
                        System.out.println(ttlOut.toString("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("STATEMENTS TO DELETE");
                    System.out.println("PREFIX qudt: <http://qudt.org/schema/qudt/>");
                    System.out.println("DELETE { ?u qudt:conversionMultiplier ?m } ");
                    System.out.println("WHERE { ?u qudt:conversionMultiplier ?m .");
                    System.out.println("VALUES  ?u {");
                    System.out.println(
                            statementsToDelete.stream()
                                    .map(s -> s.getSubject())
                                    .filter(s -> s.isIRI())
                                    .map(s -> "<" + ((IRI) s).toString() + ">")
                                    .collect(joining("\n\t")));
                    System.out.println("}}");
                });
    }

    private static void analyzeUnitsWithDimVector(
            String dimVector, PrintStream ttlPrintStream, Model statementsToDelete, Tool tool) {
        boolean writeToStdout = false;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(outputStream)) {
            output.println(String.format("\n-------- ANALYSIS FOR %s --------", dimVector));
            StringBuilder stringBuilder = new StringBuilder();
            Set<QuantityKind> qks = SelectionHelper.getQuantityKindsByDimensionVector(dimVector);
            SelectionHelper.getUnitsByDimensionVector(dimVector).stream()
                    .map(Unit::getQuantityKinds)
                    .flatMap(Collection::stream)
                    .forEach(qks::add);
            qks.remove(Qudt.QuantityKinds.Unknown);
            if (qks.isEmpty()) {
                output.println(
                        "No quantity kinds or only qk:Unknown associated with this dimension vector");
            } else {
                QuantityKindTree.makeAndFormatQuantityKindAndUnitTree(
                        qks, node -> formatTreeNode(node), stringBuilder);
                output.println("Quantity kinds and units with dimension vector " + dimVector);
                output.print(stringBuilder.toString());
            }
            Set<Unit> baseUnits = identifyBaseUnitsForDimVector(dimVector);
            if (DimensionVector.of(dimVector).map(DimensionVector::isDimensionless).orElse(false)) {
                output.println("skipping dimensionless units (too many for this check)");
                return;
            }
            output.println(
                    String.format(
                            "Identifying bases, i.e. units with Dimension Vector %s and conversionMultiplier 1.0 ...",
                            QudtNamespaces.dimensionVector.abbreviate(dimVector)));
            List<Unit> bases =
                    baseUnits.stream()
                            .sorted(Comparator.comparing(u -> u.getIri()))
                            .collect(toList());
            if (bases.isEmpty()) {
                output.println("No bases found");
            } else {
                output.println("Base candidates: " + unitCollectionToString(bases));
            }
            UnitConversionFactor[][] conversionMatrix = fullUnitConversionMatrix(bases);
            List<Unit> correctBases = findCorrectBases(bases, conversionMatrix);
            if (!correctBases.isEmpty()) {
                output.println("correct bases: " + unitCollectionToString(correctBases));
            }
            List<Unit> incorrectBases =
                    bases.stream().filter(u -> !correctBases.contains(u)).collect(toList());
            if (!incorrectBases.isEmpty()) {
                output.println("incorrect bases " + unitCollectionToString(incorrectBases));
            }
            Optional<Unit> bestBaseOpt = findBestBase(bases);
            if (bestBaseOpt.isPresent()) {
                output.println("best base unit: " + bestBaseOpt.get().getIriAbbreviated());
            }
            output.println(
                    String.format(
                            "Identifying non-base units, i.e. units with conversionMultiplier != 1.0 or without one ...",
                            QudtNamespaces.dimensionVector.abbreviate(dimVector)));
            List<Unit> nonBaseUnits = collectNonBaseUnits(dimVector, bases, incorrectBases);
            if (!nonBaseUnits.isEmpty()) {
                output.println("None-base units:" + unitCollectionToString(nonBaseUnits));
                Set<Unit> conversionImpossible = new HashSet<>();
                Set<Unit> multiplierSeemsWrong = new HashSet<>();
                Set<Unit> missingMultiplierComputed = new HashSet<>();
                Set<Unit> missingScalingOfAdded = new HashSet<>();
                ValueFactory vf = SimpleValueFactory.getInstance();
                PrintStream commentsForTTl =
                        new IndentedOutputStream(ttlPrintStream, "  # ").printStream();
                for (Unit nonBaseUnit : nonBaseUnits) {
                    if (!nonBaseUnit.isScaled()
                            && !nonBaseUnit.hasFactorUnits()
                            && !Qudt.SystemsOfUnits.SI.hasBaseUnit(nonBaseUnit)
                            && nonBaseUnit != GM
                            && nonBaseUnit.getConversionMultiplier().isPresent()) {
                        missingScalingOfAdded.add(nonBaseUnit);
                        if (bases.isEmpty()) {
                            output.println(
                                    "Found unit without isScalingOf or factorUnit, but no base to link it to: "
                                            + nonBaseUnit.getIriAbbreviated());
                        } else {
                            if (bestBaseOpt.isPresent()) {
                                ttlPrintStream.format(
                                        "%s %s %s .\n",
                                        nonBaseUnit.getIriAbbreviated(),
                                        QudtNamespaces.qudt.abbreviate(QUDT.isScalingOf.toString()),
                                        bestBaseOpt.get().getIriAbbreviated());
                            } else {
                                output.println(
                                        "Found unit without isScalingOf or factorUnit, but no base to link it to after filtering. Candidates were: "
                                                + unitCollectionToString(bases));
                            }
                        }
                    }
                    if (bestBaseOpt.isEmpty()) {
                        output.println("No unit with multiplier 1.0 qualifies as base unit");
                        if (nonBaseUnit.hasFactorUnits()) {
                            BigDecimal factor =
                                    nonBaseUnit.getFactorUnits().getConversionMultiplier();
                            if (nonBaseUnit.getConversionMultiplier().isEmpty()) {
                                missingMultiplierComputed.add(nonBaseUnit);
                                commentsForTTl.println(
                                        String.format(
                                                "%s has no conversionMultiplier, but we've found out that it is %s",
                                                nonBaseUnit.getIriAbbreviated(),
                                                factor.toString()));
                                printConversionMultiplierTriple(
                                        ttlPrintStream, commentsForTTl, nonBaseUnit, factor);
                            } else {
                                if (isRelativeDifferenceGreaterThan(
                                        factor,
                                        nonBaseUnit.getConversionMultiplier().get(),
                                        new BigDecimal("0.001"))) {
                                    multiplierSeemsWrong.add(nonBaseUnit);
                                    statementsToDelete.add(
                                            vf.createIRI(nonBaseUnit.getIri()),
                                            QUDT.conversionMultiplier,
                                            vf.createLiteral(
                                                    nonBaseUnit.getConversionMultiplier().get()));
                                    commentsForTTl.format(
                                            "%s has conversionMultiplier %s, but we've calculated it as %s (relative diff: %s - %s)\n",
                                            nonBaseUnit.getIriAbbreviated(),
                                            nonBaseUnit.getConversionMultiplier().get().toString(),
                                            factor.toString(),
                                            relativeValueDifference(
                                                            nonBaseUnit
                                                                    .getConversionMultiplier()
                                                                    .get(),
                                                            factor)
                                                    .toString(),
                                            greaterThan(
                                                            relativeValueDifference(
                                                                    factor,
                                                                    nonBaseUnit
                                                                            .getConversionMultiplier()
                                                                            .get()),
                                                            new BigDecimal("0.1"))
                                                    ? "big difference"
                                                    : "small difference");
                                    printConversionMultiplierTriple(
                                            ttlPrintStream, commentsForTTl, nonBaseUnit, factor);
                                }
                            }
                        }
                    } else {
                        Unit base = bestBaseOpt.get();
                        try {
                            BigDecimal conversionFactorToBase =
                                    nonBaseUnit.getFactorUnits().conversionFactor(base);
                            BigDecimal conversionFactorOfFactorUnits =
                                    nonBaseUnit.getFactorUnits().getConversionMultiplier();
                            if (isRelativeDifferenceGreaterThan(
                                    conversionFactorToBase,
                                    conversionFactorOfFactorUnits,
                                    new BigDecimal("0.0001"))) {
                                commentsForTTl.println(
                                        String.format(
                                                "%s converts to base %s as 1 %s = %s %s, but the conversionMultiplier calculated from its factorUnits is %s",
                                                nonBaseUnit.getIriAbbreviated(),
                                                base.getIriAbbreviated(),
                                                nonBaseUnit.toString(),
                                                conversionFactorToBase.toString(),
                                                base.toString(),
                                                conversionFactorOfFactorUnits.toString()));
                            }
                            if (nonBaseUnit.getConversionMultiplier().isEmpty()) {
                                commentsForTTl.println(
                                        String.format(
                                                "%s has no conversionMultiplier, but we've found out that 1 %s = %s %s",
                                                nonBaseUnit.getIriAbbreviated(),
                                                nonBaseUnit.toString(),
                                                conversionFactorToBase.toString(),
                                                base.toString()));
                                printConversionMultiplierTriple(
                                        ttlPrintStream,
                                        commentsForTTl,
                                        nonBaseUnit,
                                        conversionFactorToBase);
                                missingMultiplierComputed.add(nonBaseUnit);
                            } else {
                                if (isRelativeDifferenceGreaterThan(
                                        nonBaseUnit.getConversionMultiplier().get(),
                                        conversionFactorToBase,
                                        new BigDecimal("0.001"))) {
                                    multiplierSeemsWrong.add(nonBaseUnit);
                                    statementsToDelete.add(
                                            vf.createIRI(nonBaseUnit.getIri()),
                                            QUDT.conversionMultiplier,
                                            vf.createLiteral(
                                                    nonBaseUnit.getConversionMultiplier().get()));
                                    commentsForTTl.format(
                                            "%s has conversionMultiplier %s, but we've found out that 1 %s = %s %s (relative diff: %s - %s)\n",
                                            nonBaseUnit.getIriAbbreviated(),
                                            nonBaseUnit.getConversionMultiplier().get().toString(),
                                            nonBaseUnit.toString(),
                                            conversionFactorToBase.toString(),
                                            base.toString(),
                                            relativeValueDifference(
                                                            nonBaseUnit
                                                                    .getConversionMultiplier()
                                                                    .get(),
                                                            conversionFactorToBase)
                                                    .toString(),
                                            greaterThan(
                                                            relativeValueDifference(
                                                                    conversionFactorToBase,
                                                                    nonBaseUnit
                                                                            .getConversionMultiplier()
                                                                            .get()),
                                                            new BigDecimal("0.1"))
                                                    ? "big difference"
                                                    : "small difference");
                                    printConversionMultiplierTriple(
                                            ttlPrintStream,
                                            commentsForTTl,
                                            nonBaseUnit,
                                            conversionFactorToBase);
                                }
                            }
                        } catch (Exception e) {
                            output.println(
                                    String.format(
                                            "cannot convert %s to %s - %s",
                                            nonBaseUnit.getIriAbbreviated(),
                                            base.getIriAbbreviated(),
                                            e.getMessage()));
                            conversionImpossible.add(nonBaseUnit);
                        }
                    }
                }
                output.println("RESULT OF ANALYSIS:");
                boolean allCorrect =
                        conversionImpossible.isEmpty()
                                && multiplierSeemsWrong.isEmpty()
                                && missingMultiplierComputed.isEmpty()
                                && missingScalingOfAdded.isEmpty();
                if (allCorrect) {
                    output.println("all non-base units seem correct");
                } else {
                    writeToStdout = true;
                    if (!conversionImpossible.isEmpty()) {
                        output.println(
                                " - conversion impossible: "
                                        + unitCollectionToString(conversionImpossible));
                    }
                    if (!multiplierSeemsWrong.isEmpty()) {
                        output.println(
                                " - multiplier seems off: "
                                        + unitCollectionToString(multiplierSeemsWrong));
                    }
                    if (!missingMultiplierComputed.isEmpty()) {
                        output.println(
                                " - missing multiplier computed: "
                                        + unitCollectionToString(missingMultiplierComputed));
                    }
                    if (!missingScalingOfAdded.isEmpty()) {
                        output.println(
                                " - missing scalingOf (possibly added): "
                                        + unitCollectionToString(missingScalingOfAdded));
                    }
                }
            } else {
                output.println("No non-base units found");
            }
            if (writeToStdout) {
                try {
                    System.out.println(outputStream.toString("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void printConversionMultiplierTriple(
            PrintStream printStream,
            PrintStream commentStream,
            Unit nonBaseUnit,
            BigDecimal conversionFactorToBase) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df =
                new DecimalFormat(
                        "0.0#####################################################################################################################################################",
                        symbols);
        String factorUnitTree =
                UnitTree.formatFactorUnitTree(
                        nonBaseUnit,
                        factorUnit -> {
                            StringBuilder sb =
                                    new StringBuilder()
                                            .append(factorUnit.getUnit().getIriAbbreviated())
                                            .append(
                                                    factorUnit.getExponent() == 1
                                                            ? ""
                                                            : "^" + factorUnit.getExponent())
                                            .append(" multiplier: ")
                                            .append(
                                                    factorUnit
                                                            .getUnit()
                                                            .getConversionMultiplier()
                                                            .map(m -> df.format(m))
                                                            .orElse("[no conversionMultiplier]"));
                            if (factorUnit.getUnit().equals(nonBaseUnit)) {
                                sb.append(
                                        String.format(
                                                " (correct: %s)",
                                                df.format(conversionFactorToBase)));
                            }
                            if (factorUnit.getExponent() != 1) {
                                sb.append(" (multiplier")
                                        .append(
                                                factorUnit.getExponent() == 1
                                                        ? ""
                                                        : "^" + factorUnit.getExponent())
                                        .append(": ")
                                        .append(
                                                factorUnit
                                                        .getUnit()
                                                        .getConversionMultiplier()
                                                        .map(
                                                                m ->
                                                                        m.pow(
                                                                                factorUnit
                                                                                        .getExponent(),
                                                                                MathContext
                                                                                        .DECIMAL128))
                                                        .map(m -> df.format(m))
                                                        .orElse("[no conversionMultiplier]"))
                                        .append(")");
                            }
                            return sb.toString();
                        });
        commentStream.print(factorUnitTree);
        printStream.format(
                "%s %s %s .\n\n",
                nonBaseUnit.getIriAbbreviated(),
                QudtNamespaces.qudt.abbreviate(QUDT.conversionMultiplier.toString()),
                df.format(conversionFactorToBase));
    }

    private static Optional<Unit> findBestBase(List<Unit> bases) {
        return bases.stream()
                .filter(u -> Qudt.SystemsOfUnits.SI.allowsUnit(u))
                .sorted(Comparator.comparing(u -> countFactorUnits(u)))
                .findFirst();
    }

    private static int countFactorUnits(Unit unit) {
        if (unit.hasFactorUnits()) {
            return 1
                    + unit.getFactorUnits().getFactorUnits().stream()
                            .mapToInt(u -> countFactorUnits(u.getUnit()))
                            .sum();
        } else {
            return 1;
        }
    }

    private static String formatTreeNode(Node node) {
        Object data = node.getData();
        if (data instanceof Unit) {
            Unit u = (Unit) data;
            return String.format(
                    "%s (%s) %s",
                    u.getIriAbbreviated(),
                    u.getConversionMultiplier()
                            .map(m -> m.toString())
                            .orElse("[NO CONVERSION MULTIPLIER]"),
                    u.isDeprecated() ? "[deprecated]" : "");
        } else if (data instanceof QuantityKind) {
            QuantityKind qk = (QuantityKind) data;
            return QudtNamespaces.quantityKind.abbreviate(qk.getIri());
        } else {
            return data.toString();
        }
    }

    private static String unitCollectionToString(Collection<Unit> bases) {
        return bases.stream().map(Unit::getIriAbbreviated).collect(joining(", "));
    }

    private static List<Unit> collectNonBaseUnits(
            String dimVector, List<Unit> bases, List<Unit> incorrectBases) {
        List<Unit> otherUnits =
                Qudt.allUnits().stream()
                        .filter(u -> !u.isDeprecated())
                        .filter(u -> u.getDimensionVectorIri().orElse("[none]").equals(dimVector))
                        .filter(
                                u ->
                                        u.getConversionMultiplier().isEmpty()
                                                || u.getConversionMultiplier()
                                                                .get()
                                                                .compareTo(BigDecimal.ZERO)
                                                        != 0)
                        .filter(u -> !bases.contains(u))
                        .collect(toList());
        otherUnits.addAll(incorrectBases);
        return otherUnits;
    }

    private static UnitConversionFactor[][] fullUnitConversionMatrix(List<Unit> bases) {
        int nBases = bases.size();
        UnitConversionFactor[][] conversions = new UnitConversionFactor[nBases][nBases];
        for (int i = 0; i < nBases; i++) {
            Unit from = bases.get(i);
            Set<UnitConversionFactor> unitFactors = new HashSet<>();
            for (int j = 0; j < nBases; j++) {
                Unit to = bases.get(j);
                UnitConversionFactor unitFactor;
                try {
                    if (i == j) {
                        unitFactor =
                                new UnitConversionFactor(
                                        to, from.getFactorUnits().conversionFactor(to));
                    } else {
                        BigDecimal factor = from.getFactorUnits().conversionFactor(to);
                        unitFactor = new UnitConversionFactor(to, factor);
                    }
                } catch (Exception e) {
                    unitFactor = new UnitConversionFailed(to, BigDecimal.ZERO, e.getMessage());
                }
                conversions[i][j] = unitFactor;
            }
        }
        return conversions;
    }

    private static Set<Unit> identifyBaseUnitsForDimVector(String dimVector) {
        Set<Unit> baseUnits =
                Qudt.allUnits().stream()
                        .filter(u -> !u.isDeprecated())
                        .filter(
                                u ->
                                        u.getDimensionVectorIri()
                                                .orElse(NO_DIM_VECTOR)
                                                .equals(dimVector))
                        .filter(
                                u ->
                                        u.getConversionMultiplier()
                                                .map(m -> m.compareTo(BigDecimal.ONE) == 0)
                                                .orElse(false))
                        .collect(toSet());
        return baseUnits;
    }

    private static Set<String> findAllDimensionVectors() {
        return Stream.concat(
                        Qudt.allUnits().stream()
                                .map(q -> q.getDimensionVectorIri().orElse(NO_DIM_VECTOR)),
                        Qudt.allQuantityKinds().stream()
                                .map(q -> q.getDimensionVectorIri().orElse(NO_DIM_VECTOR)))
                .collect(toSet());
    }

    private static List<Unit> findCorrectBases(
            List<Unit> bases, UnitConversionFactor[][] conversions) {
        int nBases = bases.size();
        List<Integer> incorrectBaseIndices = new ArrayList<>();
        for (int rounds = 0; rounds < nBases; rounds++) {
            int[] numCorrectConversions = new int[nBases];
            for (int i = 0; i < nBases; i++) {
                if (incorrectBaseIndices.contains(i)) {
                    continue;
                }
                int numCorrectConversionsI = 0;
                for (int j = 0; j < nBases; j++) {
                    if (incorrectBaseIndices.contains(j)) {
                        continue;
                    }
                    if (conversions[i][j].factor.compareTo(BigDecimal.ONE) == 0) {
                        numCorrectConversionsI += 1;
                    }
                }
                numCorrectConversions[i] = numCorrectConversionsI;
            }
            int worstUnit = findMinIndex(numCorrectConversions, incorrectBaseIndices);
            int numCorrectInWorst = numCorrectConversions[worstUnit];
            if (numCorrectInWorst >= nBases - incorrectBaseIndices.size()) {
                return IntStream.range(0, nBases)
                        .filter(i -> !incorrectBaseIndices.contains(i))
                        .mapToObj(bases::get)
                        .collect(toList());
            }
            incorrectBaseIndices.add(worstUnit);
        }
        return List.of();
    }

    private static int findMinIndex(int[] values, List<Integer> ignoreIndices) {
        int minVal = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (ignoreIndices.contains(i)) {
                continue;
            }
            if (values[i] < minVal) {
                minIndex = i;
                minVal = values[i];
            }
        }
        return minIndex;
    }

    /**
     * Returns the difference between the two values in relation to the value of their mean
     *
     * @return
     */
    static BigDecimal relativeValueDifference(BigDecimal left, BigDecimal right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        BigDecimal mean =
                left.add(right)
                        .divide(BigDecimal.valueOf(2), MathContext.DECIMAL128)
                        .abs(MathContext.DECIMAL128);
        BigDecimal diff =
                left.abs(MathContext.DECIMAL128)
                        .subtract(right.abs(MathContext.DECIMAL128))
                        .abs(MathContext.DECIMAL128);
        return diff.divide(mean, MathContext.DECIMAL128).abs();
    }

    static boolean greaterThan(BigDecimal left, BigDecimal right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        return left.subtract(right).signum() > 0;
    }
}
