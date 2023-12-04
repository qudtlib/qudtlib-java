package io.github.qudtlib.tools.contribute.support.tree;

import java.util.Objects;

class TreeWalker<T> {
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
        for (Node<T> child : node.getChildren()) {
            walkDepthFirstInternal(child, visitor);
        }
        visitor.exit(node);
    }
}
