package io.github.qudtlib.tools.contribute.support.tree;

import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.util.function.Function;

public class UnitTree {

    public static String formatFactorUnitTree(Unit forUnit) {
        return formatFactorUnitTree(forUnit, null);
    }

    public static String formatFactorUnitTree(
            Unit forUnit, Function<FactorUnit, String> optionalFormatter) {
        StringBuilder sb = new StringBuilder();
        NodeVisitor formatter =
                new FormattingNodeVisitor<FactorUnit>(sb)
                        .nodeFormatDefault(
                                node -> {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    if (optionalFormatter != null) {
                                        stringBuilder.append(
                                                optionalFormatter.apply(node.getData()));
                                    } else {
                                        String asString =
                                                String.format("%-10s", node.getData().toString());
                                        stringBuilder
                                                .append(asString)
                                                .append(" ")
                                                .append(
                                                        node.getData()
                                                                .getDimensionVectorIri()
                                                                .orElse(
                                                                        "[missing dimension vector]"));
                                    }
                                    return stringBuilder.toString();
                                });
        Node<FactorUnit> root = buildFactorUnitTree(FactorUnit.ofUnit(forUnit));
        TreeWalker walker = new TreeWalker<FactorUnit>(root);
        walker.walkDepthFirst(formatter);
        return sb.toString();
    }

    public static Node<FactorUnit> buildFactorUnitTree(FactorUnit forUnit) {
        Node<FactorUnit> node = new Node<>(forUnit);
        Unit unitForRecursion = node.getData().getUnit();
        if (!unitForRecursion.hasFactorUnits()) {
            if (unitForRecursion.isScaled()) {
                unitForRecursion = unitForRecursion.getScalingOf().get();
            }
        }
        if (unitForRecursion.hasFactorUnits()) {
            for (FactorUnit fu : unitForRecursion.getFactorUnits()) {
                node.addChild(buildFactorUnitTree(fu));
            }
        }
        return node;
    }
}
