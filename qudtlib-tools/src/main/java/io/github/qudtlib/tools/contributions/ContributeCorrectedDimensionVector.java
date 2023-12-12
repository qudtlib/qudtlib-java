package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.tree.FormattingNodeVisitor;
import io.github.qudtlib.tools.contribute.support.tree.Node;
import io.github.qudtlib.tools.contribute.support.tree.QuantityKindTree;
import io.github.qudtlib.tools.contribute.support.tree.TreeWalker;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class ContributeCorrectedDimensionVector {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        Map<Unit, String> newDimVectors = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .forEach(
                                    u -> {
                                        String dimVector = null;
                                        FactorUnits factorUnits = null;
                                        List<FactorUnit> factorUnitList = u.getFactorUnits();
                                        factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLabel(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        try {
                                            dimVector = factorUnits.getDimensionVectorIri();
                                            final String finalDimVector = dimVector;
                                            if (dimVector != null) {
                                                if ((u.getDimensionVectorIri().isEmpty()
                                                        || !u.getDimensionVectorIri()
                                                                .get()
                                                                .equals(dimVector))) {
                                                    if (u.getDimensionVectorIri().isPresent()) {
                                                        System.err.println(
                                                                String.format(
                                                                        "wrong dimension vector on %s %s will be replaced by %s",
                                                                        u.getIriAbbreviated(),
                                                                        u.getDimensionVectorIri()
                                                                                .get(),
                                                                        dimVector));
                                                    } else {
                                                        System.err.println(
                                                                String.format(
                                                                        "missing dimension vector on %s will be set to %s",
                                                                        u.getIriAbbreviated(),
                                                                        dimVector));
                                                    }
                                                    newDimVectors.put(u, dimVector);
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
        newDimVectors.entrySet().stream()
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
                                        String correctedDimensionVector = e.getValue();
                                        out.println(
                                                String.format(
                                                        "%s has incorrect dimension vector:\n\t%s, which should be \n\t%s",
                                                        unit.getIriAbbreviated(),
                                                        unit.getDimensionVectorIri()
                                                                .orElse("[no dim vector]"),
                                                        correctedDimensionVector));
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
                                                                u.getDimensionVectorIri()
                                                                        .orElse("[no dim vector]")),
                                                out);

                                        Set<QuantityKind> quantityKindSet = unit.getQuantityKinds();
                                        out.println(
                                                String.format(
                                                        "the unit has these quantityKinds: %s",
                                                        quantityKindSet.stream()
                                                                .map(QuantityKind::getIriLocalname)
                                                                .collect(
                                                                        Collectors.joining(", "))));

                                        for (QuantityKind qk : quantityKindSet) {
                                            List<Unit> otherUnits =
                                                    getUnitsAssociatedWithQuantityKind(qk);
                                            otherUnits.remove(unit);
                                            out.println(
                                                    String.format(
                                                            "\tquantityKind %s, has %d other units associated",
                                                            qk.getIriLocalname(),
                                                            otherUnits.size()));
                                            if (!(otherUnits.isEmpty()
                                                    || qk.getIriLocalname().equals("Unknown"))) {
                                                for (Unit otherUnit : otherUnits) {
                                                    out.println(
                                                            String.format(
                                                                    "\t\t%-40s %s",
                                                                    otherUnit.getIriAbbreviated(),
                                                                    otherUnit
                                                                            .getDimensionVectorIri()
                                                                            .orElse(
                                                                                    "[no dim vector]")));
                                                }
                                            }
                                        }
                                        List<QuantityKind> fittingQuantityKinds =
                                                getQuantityKindsByDimensionVector(
                                                        correctedDimensionVector);
                                        List<Node<QuantityKind>> quantityKindForest =
                                                QuantityKindTree.makeSkosBroaderForest(
                                                        fittingQuantityKinds);
                                        List<Node<Object>> quantityKindsWithUnits =
                                                QuantityKindTree
                                                        .addAssociatedUnitsToQuantityKindForest(
                                                                quantityKindForest);

                                        out.println(
                                                String.format(
                                                        "quantity kinds that fit the corrected dimension vector %s, and associated units:",
                                                        correctedDimensionVector));
                                        for (Node<Object> root : quantityKindsWithUnits) {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            FormattingNodeVisitor formatter =
                                                    new FormattingNodeVisitor<Object>(
                                                            stringBuilder,
                                                            (sb, node) -> {
                                                                sb.append(
                                                                        (node.getData()
                                                                                        instanceof
                                                                                        Unit)
                                                                                ? ((Unit)
                                                                                                node
                                                                                                        .getData())
                                                                                        .getIriAbbreviated()
                                                                                : QudtNamespaces
                                                                                        .quantityKind
                                                                                        .abbreviate(
                                                                                                ((QuantityKind)
                                                                                                                node
                                                                                                                        .getData())
                                                                                                        .getIri()));
                                                            });
                                            TreeWalker.of(root).walkDepthFirst(formatter);
                                            out.print(stringBuilder.toString());
                                        }
                                        System.out.println(
                                                String.format(
                                                        "%s qudt:hasDimensionVector %s .",
                                                        unit.getIriAbbreviated(),
                                                        QudtNamespaces.dimensionVector.abbreviate(
                                                                correctedDimensionVector)));

                                        System.out.println();
                                    });
                        });
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
