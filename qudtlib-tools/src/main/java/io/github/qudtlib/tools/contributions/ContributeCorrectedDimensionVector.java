package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.tools.contribute.support.tree.FormattingNodeVisitor.*;

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

                                        if (quantityKindSet.equals(Set.of(QuantityKinds.Unknown))) {
                                            out.println(" (omitting unit/quantity kind tree)");
                                        } else {
                                            List<Node<QuantityKind>> actualQuantityKindForest =
                                                    QuantityKindTree
                                                            .makeCompleteSkosBroaderForestContaining(
                                                                    quantityKindSet);
                                            List<Node<Object>> actualQuantityKindsWithUnits =
                                                    QuantityKindTree
                                                            .addAssociatedUnitsToQuantityKindForest(
                                                                    actualQuantityKindForest);
                                            for (Node<Object> root : actualQuantityKindsWithUnits) {
                                                StringBuilder stringBuilder = new StringBuilder();
                                                FormattingNodeVisitor<Object> formatter =
                                                        new FormattingNodeVisitor<>(stringBuilder)
                                                                .nodeFormatDefault(
                                                                        node ->
                                                                                getUnitOrQuantityKindIri(
                                                                                        node));
                                                formatter.treeDrawingConfig(
                                                        (config, node) -> {
                                                            if (node.getNode().getData()
                                                                    instanceof QuantityKind) {
                                                                config.branchStart(
                                                                                CHAR_SLIM_DOUBLE_RIGHT_T)
                                                                        .branchStartLast(
                                                                                CHAR_SLIM_DOUBLE_ANGLE)
                                                                        .branch(CHAR_DOUBLE_HORIZ)
                                                                        .branchEndLeaf(
                                                                                CHAR_DOUBLE_HORIZ)
                                                                        .branchEndInner(
                                                                                CHAR_DOUBLE_SLIM_T);
                                                            }
                                                        });
                                                TreeWalker.of(root)
                                                        .sorted(
                                                                Comparator.comparing(
                                                                                (Node<Object>
                                                                                                node) ->
                                                                                        node.getData()
                                                                                                .getClass()
                                                                                                .getSimpleName())
                                                                        .thenComparing(
                                                                                ContributeCorrectedDimensionVector
                                                                                        ::getUnitOrQuantityKindIri))
                                                        .walkDepthFirst(formatter);
                                                out.print(stringBuilder.toString());
                                            }
                                        }
                                        List<QuantityKind> fittingQuantityKinds =
                                                getQuantityKindsByDimensionVector(
                                                        correctedDimensionVector);
                                        List<Node<QuantityKind>> quantityKindForest =
                                                QuantityKindTree
                                                        .makeCompleteSkosBroaderForestContaining(
                                                                fittingQuantityKinds);
                                        List<Node<Object>> quantityKindsWithUnits =
                                                QuantityKindTree
                                                        .addAssociatedUnitsToQuantityKindForest(
                                                                quantityKindForest);

                                        out.println(
                                                String.format(
                                                        "quantity kinds that fit the corrected dimension vector %s, and associated units:",
                                                        correctedDimensionVector));
                                        if (quantityKindsWithUnits.isEmpty()) {
                                            out.println("(none found)");
                                        } else {
                                            for (Node<Object> root : quantityKindsWithUnits) {
                                                StringBuilder stringBuilder = new StringBuilder();
                                                FormattingNodeVisitor<Object> formatter =
                                                        new FormattingNodeVisitor<>(stringBuilder)
                                                                .nodeFormatDefault(
                                                                        node ->
                                                                                getUnitOrQuantityKindIri(
                                                                                        node));
                                                formatter.treeDrawingConfig(
                                                        (config, node) -> {
                                                            if (node.getNode().getData()
                                                                    instanceof QuantityKind) {
                                                                config.branchStart(
                                                                                CHAR_SLIM_DOUBLE_RIGHT_T)
                                                                        .branchStartLast(
                                                                                CHAR_SLIM_DOUBLE_ANGLE)
                                                                        .branch(CHAR_DOUBLE_HORIZ)
                                                                        .branchEndLeaf(
                                                                                CHAR_DOUBLE_HORIZ)
                                                                        .branchEndInner(
                                                                                CHAR_DOUBLE_SLIM_T);
                                                            }
                                                        });
                                                TreeWalker.of(root)
                                                        .sorted(
                                                                Comparator.comparing(
                                                                                (Node<Object>
                                                                                                node) ->
                                                                                        node.getData()
                                                                                                .getClass()
                                                                                                .getSimpleName())
                                                                        .thenComparing(
                                                                                ContributeCorrectedDimensionVector
                                                                                        ::getUnitOrQuantityKindIri))
                                                        .walkDepthFirst(formatter);
                                                out.print(stringBuilder.toString());
                                            }
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
