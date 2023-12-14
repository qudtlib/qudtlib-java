package io.github.qudtlib.tools.contribute.support.tree;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.QudtNamespaces;
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
                                    sb.append(getShortName(node.getData()));
                                    if (!node.getData().getExactMatches().isEmpty()) {
                                        sb.append(" - exact matches: ")
                                                .append(
                                                        node.getData().getExactMatches().stream()
                                                                .map(qk -> getShortName(qk))
                                                                .collect(Collectors.joining(",")));
                                    }
                                    return sb.toString();
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
}
