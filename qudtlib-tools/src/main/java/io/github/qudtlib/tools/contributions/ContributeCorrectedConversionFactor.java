package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.tools.contribute.support.tree.FormattingNodeVisitor.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.tree.Node;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * * if a derived unit has a conversionMultiplier, the base units should have one, too. (just calculate, assuming all involved factors are correct. If more than one missing, manual change required)
 * * if the base units have a conversionMultiplier, the derived units should have one too. (just calculate, assuming all involved factors are correct. If more than one missing, manual change required)
 * * if there is a mismatch between the calculated cM of a derived unit and its actual cM, manual change required
 */
public class ContributeCorrectedConversionFactor {
    private static class UnitFactor {
        private Unit unit;
        private BigDecimal factor;

        public UnitFactor(Unit unit, BigDecimal factor) {
            this.unit = unit;
            this.factor = factor;
        }
    }

    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        Map<Unit, UnitFactor> newConversionMultipliers = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .forEach(
                                    u -> {
                                        BigDecimal conversionMultiplier = null;
                                        FactorUnits factorUnits = null;
                                        List<FactorUnit> factorUnitList = u.getFactorUnits();
                                        factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLabel(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        final FactorUnits finalFactorUnits = factorUnits;
                                        try {
                                            String dimVector = factorUnits.getDimensionVectorIri();
                                            Unit base =
                                                    Qudt.allUnits().stream()
                                                            .filter(
                                                                    b ->
                                                                            b.getDimensionVectorIri()
                                                                                    .isPresent())
                                                            .filter(
                                                                    b ->
                                                                            b.getDimensionVectorIri()
                                                                                    .get()
                                                                                    .equals(
                                                                                            dimVector))
                                                            .filter(
                                                                    b ->
                                                                            b.getConversionMultiplier()
                                                                                    .isPresent())
                                                            .filter(
                                                                    b ->
                                                                            b.getConversionMultiplier()
                                                                                            .get()
                                                                                            .compareTo(
                                                                                                    BigDecimal
                                                                                                            .ONE)
                                                                                    == 0)
                                                            .sorted(
                                                                    Comparator.comparing(
                                                                            (Unit un) ->
                                                                                    countSIBaseUnits(
                                                                                            un)))
                                                            .findFirst()
                                                            .get();
                                            conversionMultiplier =
                                                    finalFactorUnits.conversionFactor(base);
                                            System.err.println(
                                                    String.format(
                                                            "multiplier %s to %s: %s ",
                                                            u.getIriAbbreviated(),
                                                            base.getIriAbbreviated(),
                                                            conversionMultiplier.toString()));

                                            if (conversionMultiplier != null) {
                                                if ((u.getConversionMultiplier().isEmpty()
                                                        || u.getConversionMultiplier()
                                                                        .get()
                                                                        .abs(MathContext.DECIMAL128)
                                                                        .subtract(
                                                                                conversionMultiplier
                                                                                        .abs(
                                                                                                MathContext
                                                                                                        .DECIMAL128))
                                                                        .abs(MathContext.DECIMAL128)
                                                                        .compareTo(
                                                                                BigDecimal.valueOf(
                                                                                        0.1))
                                                                > 0)) {
                                                    if (u.getConversionMultiplier().isPresent()) {
                                                        System.err.println(
                                                                String.format(
                                                                        "wrong conversion multiplier on %s %s will be replaced by %s",
                                                                        u.getIriAbbreviated(),
                                                                        u.getConversionMultiplier()
                                                                                .get(),
                                                                        conversionMultiplier));
                                                    } else {
                                                        System.err.println(
                                                                String.format(
                                                                        "missing conversion multiplier on %s will be set to %s",
                                                                        u.getConversionMultiplier(),
                                                                        conversionMultiplier));
                                                    }
                                                    newConversionMultipliers.put(
                                                            u,
                                                            new UnitFactor(
                                                                    base, conversionMultiplier));
                                                }
                                            } else {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot check/fix dim vector for %s: one of the constituent units has no dim vector",
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
        newConversionMultipliers.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getIri()))
                .forEach(
                        e -> {
                            entityGenerator.unitOfWork(
                                    tool -> {
                                        PrintStream out =
                                                new PrintStream(
                                                        new IndentedOutputStream(
                                                                System.out, "  # "));
                                        Unit unit = e.getKey();
                                        UnitFactor correctedConversionMultiplier = e.getValue();
                                        out.println(
                                                String.format(
                                                        "%s has incorrect conversion multiplier (converting toward unit %s) :\n\t%s, which should be \n\t%s",
                                                        unit.getIriAbbreviated(),
                                                        correctedConversionMultiplier.unit
                                                                .getIriAbbreviated(),
                                                        unit.getConversionMultiplier()
                                                                .map(Object::toString)
                                                                .orElse("[none]"),
                                                        correctedConversionMultiplier.factor));
                                        tool.printFactorUnitTree(
                                                unit,
                                                u ->
                                                        String.format(
                                                                "%-30s\t%s",
                                                                (u.getUnit().getIriAbbreviated()
                                                                        + (u.getExponent() == 1
                                                                                ? ""
                                                                                : "^"
                                                                                        + u
                                                                                                .getExponent())),
                                                                u.getUnit()
                                                                        .getConversionMultiplier()
                                                                        .map(Object::toString)
                                                                        .orElse("[none]")),
                                                out);

                                        System.out.println(
                                                String.format(
                                                        "%s qudt:hasConversionMultiplier %s .",
                                                        unit.getIriAbbreviated(),
                                                        correctedConversionMultiplier.factor
                                                                .toString()));

                                        System.out.println();
                                    });
                        });
    }

    private static long countSIBaseUnits(Unit l) {
        return l.getFactorUnits().stream()
                .filter(fu -> SystemsOfUnits.SI.hasBaseUnit(fu.getUnit()))
                .count();
    }

    private static String getUnitOrQuantityKindIri(Node<?> node) {
        return (node.getData() instanceof Unit)
                ? ((Unit) node.getData()).getIriAbbreviated()
                : QudtNamespaces.quantityKind.abbreviate(((QuantityKind) node.getData()).getIri());
    }

    private static List<Unit> getUnitsAssociatedWithQuantityKind(QuantityKind quantityKind) {
        return Qudt.allUnits().stream()
                .filter(u -> u.getQuantityKinds().contains(quantityKind))
                .collect(Collectors.toList());
    }

    private static List<QuantityKind> getQuantityKindsByDimensionVector(String dimensionVectorIri) {
        return Qudt.allQuantityKinds().stream()
                .filter(
                        qk ->
                                qk.getDimensionVectorIri()
                                        .map(dv -> dv.equals(dimensionVectorIri))
                                        .orElse(false))
                .collect(Collectors.toList());
    }

    private static boolean hasOtherUnitsAssociated(QuantityKind quantityKind, Unit except) {
        List<Unit> ret = getUnitsAssociatedWithQuantityKind(quantityKind);
        ret.remove(except);
        return !ret.isEmpty();
    }

    private static List<Unit> getUnitsByDimensionVector(String dimensionVectorIri) {
        return Qudt.allUnits().stream()
                .filter(
                        u ->
                                u.getDimensionVectorIri()
                                        .map(dv -> dv.equals(dimensionVectorIri))
                                        .orElse(false))
                .collect(Collectors.toList());
    }
}
