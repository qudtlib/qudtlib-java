package io.github.qudtlib.tools.contribute.support.tree;

import static io.github.qudtlib.tools.contribute.support.tree.FormattingNodeVisitor.*;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.model.Unit;
import java.util.*;
import java.util.stream.Collectors;

public class QuantityKindTree {
    public static String formatQuantityKindForest(Collection<QuantityKind> quantityKinds) {
        return formatSkosBroaderForest(makeSkosBroaderForest(quantityKinds));
    }

    public static String formatSkosBroaderForest(List<Node<QuantityKind>> forest) {
        StringBuilder sb = new StringBuilder();
        NodeVisitor<QuantityKind> formattingVisitor =
                new FormattingNodeVisitor<QuantityKind>(sb)
                        .nodeFormatDefault(
                                node -> {
                                    StringBuilder ret = new StringBuilder();
                                    ret.append(getShortName(node.getData()));
                                    if (!node.getData().getExactMatches().isEmpty()) {
                                        ret.append(" - exact matches: ")
                                                .append(
                                                        node.getData().getExactMatches().stream()
                                                                .map(qk -> getShortName(qk))
                                                                .collect(Collectors.joining(",")));
                                    }
                                    return ret.toString();
                                });
        for (Node<QuantityKind> tree : forest) {
            TreeWalker.of(tree).walkDepthFirst(formattingVisitor);
        }
        return sb.toString();
    }

    public static List<Node<QuantityKind>> makeSkosBroaderForest(
            Collection<QuantityKind> quantityKinds) {
        List<Node<QuantityKind>> resultForest =
                Node.forestOf(
                        quantityKinds,
                        (parentCandidate, childCandidate) ->
                                childCandidate.getBroaderQuantityKinds().contains(parentCandidate));
        return resultForest;
    }

    public static List<Node<QuantityKind>> makeCompleteSkosBroaderForestContaining(
            Collection<QuantityKind> quantityKinds) {
        Set<QuantityKind> transitiveHull = new HashSet<>(quantityKinds);
        int hullSize = -1;
        while (transitiveHull.size() > hullSize) {
            hullSize = transitiveHull.size();
            Set<QuantityKind> toAdd = new HashSet<>();
            for (QuantityKind qk : transitiveHull) {
                toAdd.addAll(qk.getBroaderQuantityKinds());
            }
            transitiveHull.addAll(toAdd);
        }
        Qudt.allQuantityKinds().stream()
                .filter(
                        qk ->
                                qk.getBroaderQuantityKinds().stream()
                                        .anyMatch(q -> transitiveHull.contains(q)))
                .forEach(transitiveHull::add);
        List<Node<QuantityKind>> resultForest =
                Node.forestOf(
                        transitiveHull,
                        (parentCandidate, childCandidate) ->
                                childCandidate.getBroaderQuantityKinds().contains(parentCandidate));
        return resultForest;
    }

    public static List<Node<Object>> addAssociatedUnitsToQuantityKindForest(
            List<Node<QuantityKind>> forest) {
        List<Node<Object>> qksWithUnits = new ArrayList<>();
        for (Node<QuantityKind> node : forest) {
            qksWithUnits.add(addUnits(node));
        }
        return qksWithUnits;
    }

    private static Node<Object> addUnits(Node<QuantityKind> node) {
        Node<Object> thisNode = new Node<>(node.getData());
        for (Node<QuantityKind> child : node.getChildren()) {
            thisNode.addChild(addUnits(child));
        }
        Qudt.allUnits().stream()
                .filter(u -> u.getQuantityKinds().contains(node.getData()))
                .forEach(
                        unit -> {
                            thisNode.addChild(new Node<>(unit));
                        });
        // node.getData().getApplicableUnits().forEach(u -> thisNode.addChild(u));
        return thisNode;
    }

    private static String getShortName(QuantityKind quantityKind) {
        return QudtNamespaces.quantityKind.abbreviate(quantityKind.getIri());
    }

    public static void formatQuantityKindTree(
            List<Node<Object>> actualQuantityKindsWithUnits, StringBuilder stringBuilder) {
        formatQuantityKindTree(actualQuantityKindsWithUnits, null, stringBuilder);
    }

    public static void formatQuantityKindTree(
            List<Node<Object>> actualQuantityKindsWithUnits,
            NodeToString<Object> nodeFormatter,
            StringBuilder stringBuilder) {
        if (nodeFormatter == null) {
            nodeFormatter = node -> getUnitOrQuantityKindIriAbbreviated(node);
        }
        for (Node<Object> root : actualQuantityKindsWithUnits) {
            FormattingNodeVisitor<Object> formatter =
                    new FormattingNodeVisitor<Object>(stringBuilder)
                            .nodeFormatDefault(nodeFormatter);
            formatter.treeDrawingConfig(
                    (config, node) -> {
                        if (node.getNode().getData() instanceof QuantityKind) {
                            config.branchStart(CHAR_SLIM_DOUBLE_RIGHT_T)
                                    .branchStartLast(CHAR_SLIM_DOUBLE_ANGLE)
                                    .branch(CHAR_DOUBLE_HORIZ)
                                    .branchEndLeaf(CHAR_DOUBLE_HORIZ)
                                    .branchEndInner(CHAR_DOUBLE_SLIM_T);
                        }
                    });
            TreeWalker.of(root)
                    .sorted(
                            Comparator.comparing(
                                            (Node<Object> node) ->
                                                    node.getData().getClass().getSimpleName())
                                    .thenComparing(
                                            QuantityKindTree::getUnitOrQuantityKindIriAbbreviated))
                    .walkDepthFirst(formatter);
        }
    }

    public static String getUnitOrQuantityKindIriAbbreviated(Node<?> node) {
        return (node.getData() instanceof Unit)
                ? ((Unit) node.getData()).getIriAbbreviated()
                : QudtNamespaces.quantityKind.abbreviate(((QuantityKind) node.getData()).getIri());
    }

    public static void makeAndFormatQuantityKindAndUnitTree(
            Set<QuantityKind> quantityKindSet, StringBuilder stringBuilder) {
        makeAndFormatQuantityKindAndUnitTree(quantityKindSet, null, stringBuilder);
    }

    public static void makeAndFormatQuantityKindAndUnitTree(
            Set<QuantityKind> quantityKindSet,
            NodeToString<Object> nodeFormatter,
            StringBuilder stringBuilder) {
        List<Node<QuantityKind>> actualQuantityKindForest =
                makeCompleteSkosBroaderForestContaining(quantityKindSet);
        List<Node<Object>> actualQuantityKindsWithUnits =
                addAssociatedUnitsToQuantityKindForest(actualQuantityKindForest);
        formatQuantityKindTree(actualQuantityKindsWithUnits, nodeFormatter, stringBuilder);
    }
}
