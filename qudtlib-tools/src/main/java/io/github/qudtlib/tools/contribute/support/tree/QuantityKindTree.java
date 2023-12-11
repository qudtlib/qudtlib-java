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
                new FormattingNodeVisitor<QuantityKind>(
                        sb,
                        (stringBuilder, node) -> {
                            stringBuilder.append(getShortName(node.getData()));
                            if (!node.getData().getExactMatches().isEmpty()) {
                                stringBuilder
                                        .append(" - exact matches: ")
                                        .append(
                                                node.getData().getExactMatches().stream()
                                                        .map(qk -> getShortName(qk))
                                                        .collect(Collectors.joining(",")));
                            }
                        });
        for (Node<QuantityKind> tree : forest) {
            TreeWalker.of(tree).walkDepthFirst(formattingVisitor);
        }
        return sb.toString();
    }

    public static List<Node<QuantityKind>> makeSkosBroaderForest(
            Collection<QuantityKind> quantityKinds) {
        List<Node<QuantityKind>> resultForest = new ArrayList<>();
        for (QuantityKind quantityKind : quantityKinds) {
            boolean foundParent = false;
            for (Node<QuantityKind> tree : resultForest) {
                Optional<Node<QuantityKind>> parentOpt =
                        tree.findFirst(
                                parentCandidate ->
                                        quantityKind
                                                .getBroaderQuantityKinds()
                                                .contains(parentCandidate));
                if (parentOpt.isPresent()) {
                    parentOpt.get().addChild(quantityKind);
                    foundParent = true;
                }
            }
            if (!foundParent) {
                resultForest.add(new Node<>(quantityKind));
            }
        }
        return resultForest;
    }

    public static List<Node<Object>> addAssociatedUnitsToQuantityKindForest(List<Node<QuantityKind>> forest){
        List<Node<Object>> qksWithUnits = new ArrayList<>();
        for (Node<QuantityKind> node: forest){
            qksWithUnits.add(addUnits(node));
        }
        return qksWithUnits;
    }

    private static Node<Object> addUnits(Node<QuantityKind> node) {
        Node<Object> thisNode = new Node<>(node.getData());
        for (Node<QuantityKind> child: node.getChildren()) {
            thisNode.addChild(addUnits(child));
        }
        Qudt.allUnits().stream().filter(u -> u.getQuantityKinds().contains(node.getData())).forEach(unit -> {
            thisNode.addChild(new Node<>(unit));
        });
        return thisNode;
    }

    private static String getShortName(QuantityKind quantityKind) {
        return QudtNamespaces.quantityKind.abbreviate(quantityKind.getIri());
    }
}
