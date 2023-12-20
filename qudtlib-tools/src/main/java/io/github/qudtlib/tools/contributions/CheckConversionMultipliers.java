package io.github.qudtlib.tools.contributions;

import static java.util.stream.Collectors.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.DimensionVector;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.vocab.QUDT;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.IntStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class CheckConversionMultipliers {

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
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    int totalCorrections = 0;
                    Map<String, Set<Unit>> baseUnits =
                            Qudt.allUnits().stream()
                                    .filter(
                                            u ->
                                                    u.getConversionMultiplier()
                                                            .map(
                                                                    m ->
                                                                            m.compareTo(
                                                                                            BigDecimal
                                                                                                    .ONE)
                                                                                    == 0)
                                                            .orElse(false))
                                    .collect(
                                            groupingBy(
                                                    u -> u.getDimensionVectorIri().orElse("[none]"),
                                                    toSet()));
                    for (String dimVector : baseUnits.keySet()) {
                        System.out.println(
                                String.format(
                                        "Units with Dimension Vector %s and conversionMultiplier 1.0:",
                                        QudtNamespaces.dimensionVector.abbreviate(dimVector)));
                        if (DimensionVector.of(dimVector)
                                .map(DimensionVector::isDimensionless)
                                .orElse(false)) {
                            System.out.println(
                                    "skipping dimensionless units (too many for this check)");
                            continue;
                        }
                        List<Unit> bases = new ArrayList<>(baseUnits.get(dimVector));
                        int nBases = bases.size();
                        UnitConversionFactor[][] conversions =
                                new UnitConversionFactor[nBases][nBases];
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
                                                        to,
                                                        from.getFactorUnits().conversionFactor(to));
                                    } else {
                                        BigDecimal factor =
                                                from.getFactorUnits().conversionFactor(to);
                                        unitFactor = new UnitConversionFactor(to, factor);
                                    }
                                } catch (Exception e) {
                                    System.err.println(
                                            "error while comparing bases:"
                                                    + e.getMessage()); // ignore for now
                                    unitFactor =
                                            new UnitConversionFailed(
                                                    to, BigDecimal.ZERO, e.getMessage());
                                }
                                conversions[i][j] = unitFactor;
                            }
                        }
                        List<Unit> correctBases = findCorrectBases(bases, conversions);
                        System.out.println(
                                "correct bases: "
                                        + correctBases.stream()
                                                .map(u -> u.getIriAbbreviated())
                                                .collect(joining(",")));
                        List<Unit> incorrectBases =
                                bases.stream()
                                        .filter(u -> !correctBases.contains(u))
                                        .collect(toList());
                        if (!incorrectBases.isEmpty()) {
                            System.out.println(
                                    "incorrect bases "
                                            + incorrectBases.stream()
                                                    .map(Unit::getIriAbbreviated)
                                                    .collect(joining(",")));
                        }
                        List<Unit> otherUnits =
                                Qudt.allUnits().stream()
                                        .filter(
                                                u ->
                                                        u.getDimensionVectorIri()
                                                                .orElse("[none]")
                                                                .equals(dimVector))
                                        .filter(u -> !bases.contains(u))
                                        .collect(toList());
                        if (!otherUnits.isEmpty()) {
                            ValueFactory vf = SimpleValueFactory.getInstance();
                            System.out.println(
                                    "other units:"
                                            + otherUnits.stream()
                                                    .map(Unit::getIriAbbreviated)
                                                    .collect(joining(",")));
                            boolean allCorrect = true;
                            for (Unit other : otherUnits) {
                                    for (Unit correctBase : correctBases) {
                                        try {
                                            BigDecimal calculatedFactor =
                                                    other.getFactorUnits()
                                                            .conversionFactor(correctBase);
                                            if (other.getConversionMultiplier().isEmpty()) {
                                                allCorrect = false;
                                                totalCorrections++;
                                                System.out.println(
                                                        String.format(
                                                                "%s has no conversionMultiplier, but we've found out that 1 %s = %s %s",
                                                                other.getIriAbbreviated(),
                                                                other.toString(),
                                                                calculatedFactor.toString(),
                                                                correctBase.toString()));
                                                statementsToAdd.add(
                                                        vf.createIRI(other.getIri()),
                                                        QUDT.conversionMultiplier,
                                                        vf.createLiteral(calculatedFactor));
                                            } else {
                                                if (isRelativeDifferenceGreaterThan(
                                                        other,
                                                        calculatedFactor,
                                                        new BigDecimal("0.0001"))) {
                                                    allCorrect = false;
                                                    totalCorrections++;
                                                    statementsToDelete.add(
                                                            vf.createIRI(other.getIri()),
                                                            QUDT.conversionMultiplier,
                                                            vf.createLiteral(
                                                                    other.getConversionMultiplier()
                                                                            .get()));
                                                    statementsToAdd.add(
                                                            vf.createIRI(other.getIri()),
                                                            QUDT.conversionMultiplier,
                                                            vf.createLiteral(calculatedFactor));
                                                    System.out.format(
                                                            "%s has conversionMultiplier %s, but we've found out that 1 %s = %s %s (relative diff: %s - %s)\n",
                                                            other.getIriAbbreviated(),
                                                            other.getConversionMultiplier()
                                                                    .get()
                                                                    .toString(),
                                                            other.toString(),
                                                            calculatedFactor.toString(),
                                                            correctBase.toString(),
                                                            relativeValueDifference(
                                                                            other.getConversionMultiplier()
                                                                                    .get(),
                                                                            calculatedFactor)
                                                                    .toString(),
                                                            greaterThan(
                                                                            relativeValueDifference(
                                                                                    calculatedFactor,
                                                                                    other.getConversionMultiplier()
                                                                                            .get()),
                                                                            new BigDecimal("0.1"))
                                                                    ? "big difference"
                                                                    : "small difference");
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.err.println("cannot convert: " + e.getMessage());
                                            allCorrect = false;
                                        }
                                }
                            }
                            if (allCorrect) {
                                System.out.println("   (all correct)");
                            }
                        } else {
                            System.out.println(" (no other units)");
                        }
                    }
                    System.out.format("statements to add: %d\n", statementsToAdd.size());
                    System.out.format("statements to delete: %d\n", statementsToDelete.size());
                    System.out.println("STATEMENTS TO ADD");
                    tool.writeOut(statementsToAdd, System.out, s -> true);
                    System.out.println("STATEMENTS TO DELETE");
                    tool.writeOut(statementsToDelete, System.out, s -> true);
                });
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

    private static boolean isRelativeDifferenceGreaterThan(
            Unit other, BigDecimal calculatedFactor, BigDecimal epsilon) {
        return greaterThan(
                relativeValueDifference(calculatedFactor, other.getConversionMultiplier().get()),
                epsilon);
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
