package io.github.qudtlib.tools.entitygen.support;

import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.QudtNamespaces;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuantityKindTree {
    public static String formatQuantityKindForest(Collection<QuantityKind> quantityKinds) {
        return formatSkosBroaderForest(makeSkosBroaderForest(quantityKinds));
    }

    public static String formatSkosBroaderForest(List<Node<QuantityKind>> forest) {
        StringBuilder sb = new StringBuilder();
        NodeVisitor<QuantityKind> formattingVisitor =
                new NodeVisitor<QuantityKind>() {
                    String indent = "|   ";
                    int indentSize = indent.length();
                    String currentIndent = "    +-- ";

                    @Override
                    public void enter(Node<QuantityKind> node) {
                        sb.append(currentIndent).append(getShortName(node.data));
                        if (!node.data.getExactMatches().isEmpty()) {
                            sb.append(" - exact matches: ")
                                    .append(
                                            node.data.getExactMatches().stream()
                                                    .map(qk -> getShortName(qk))
                                                    .collect(Collectors.joining(",")));
                        }
                        sb.append("\n");

                        currentIndent =
                                currentIndent.substring(0, indentSize)
                                        + indent
                                        + currentIndent.substring(indentSize);
                    }

                    private String getShortName(QuantityKind quantityKind) {
                        return QudtNamespaces.quantityKind.abbreviate(quantityKind.getIri());
                    }

                    @Override
                    public void exit(Node<QuantityKind> node) {
                        currentIndent =
                                currentIndent.substring(0, indentSize)
                                        + currentIndent.substring(indentSize + indentSize);
                    }
                };
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

    private static interface NodeVisitor<T> {
        void enter(Node<T> root);

        void exit(Node<T> root);
    }

    private static class TreeWalker<T> {
        private Node<T> root;

        public TreeWalker(Node<T> root) {
            Objects.requireNonNull(root);
            this.root = root;
        }

        public static <X> TreeWalker<X> of(Node<X> root) {
            return new TreeWalker<X>(root);
        }

        public <R> void walkDepthFirst(NodeVisitor<T> visitor) {
            walkDepthFirstInternal(root, visitor);
        }

        private <R> void walkDepthFirstInternal(Node<T> node, NodeVisitor<T> visitor) {
            visitor.enter(node);
            for (Node<T> child : node.children) {
                walkDepthFirstInternal(child, visitor);
            }
            visitor.exit(node);
        }
    }

    private static class Node<T> {
        private T data;
        private Set<Node<T>> children = new HashSet<>();

        public Node(T data) {
            this.data = data;
        }

        public Node<T> addChild(T child) {
            Node<T> newNode = new Node(child);
            this.children.add(newNode);
            return newNode;
        }

        public Optional<Node<T>> findFirst(Function<T, Boolean> predicate) {
            if (predicate.apply(this.data)) {
                return Optional.of(this);
            }
            for (Node<T> child : this.children) {
                Optional<Node<T>> result = child.findFirst(predicate);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        }
    }
}
