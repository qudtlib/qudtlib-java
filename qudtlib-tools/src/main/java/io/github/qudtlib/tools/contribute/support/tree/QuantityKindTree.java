package io.github.qudtlib.tools.contribute.support.tree;

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

    private static String getShortName(QuantityKind quantityKind) {
        return QudtNamespaces.quantityKind.abbreviate(quantityKind.getIri());
    }
}
