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
                new FormattingNodeVisitor<FactorUnit>(
                        sb,
                        (stringBuilder, node) -> {
                            if (optionalFormatter != null) {
                                stringBuilder.append(optionalFormatter.apply(node.getData()));
                            } else {
                                stringBuilder
                                        .append(node.getData().toString())
                                        .append("   ")
                                        .append(
                                                node.getData()
                                                        .getDimensionVectorIri()
                                                        .orElse("[missing dimension vector]"));
                            }
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
