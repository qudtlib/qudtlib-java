package io.github.qudtlib.tools.contribute.support.tree;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

public class TreeWalker<T> {
    private Node<T> root;
    private Comparator<Node<T>> iterationOrderComparator = null;

    public TreeWalker(Node<T> root) {
        this(root, null);
    }

    public TreeWalker(Node<T> root,
                    Comparator<Node<T>> iterationOrderComparator) {
         Objects.requireNonNull(root);
        this.root = root;
        this.iterationOrderComparator = iterationOrderComparator;
    }

    public static <X> TreeWalker<X> of(Node<X> root) {
        return TreeWalker.of(root, null);
    }

    public static <X> TreeWalker<X> of(Node<X> root, Comparator<Node<X>> comparator) {
        return new TreeWalker<X>(root, comparator);
    }

    public void walkDepthFirst(NodeVisitor<T> visitor) {
        walkDepthFirstInternal(root, visitor);
    }

    private void walkDepthFirstInternal(Node<T> node, NodeVisitor<T> visitor) {
        visitor.enter(node);
        this.iterateOverChildren(node, visitor, child -> walkDepthFirstInternal(child,visitor));
        visitor.exit(node);
    }

    private void iterateOverChildren(Node<T> node, NodeVisitor<T> visitor, Consumer<Node<T>> childHandler) {
        if (this.iterationOrderComparator != null) {
            node.getChildren().stream().sorted(this.iterationOrderComparator).forEach(childHandler::accept);
        } else {
            node.getChildren().stream().forEach(childHandler);
        }
    }

}
