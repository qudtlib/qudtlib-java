package io.github.qudtlib.tools.contribute.support.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TreeWalker<T> {
    private Node<T> root;
    private Comparator<Node<T>> iterationOrderComparator = null;

    public TreeWalker(Node<T> root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    public TreeWalker sorted(Comparator<Node<T>> comparator) {
        this.iterationOrderComparator = comparator;
        return this;
    }

    public static <X> TreeWalker<X> of(Node<X> root) {
        return new TreeWalker<X>(root);
    }

    public void walkDepthFirst(NodeVisitor<T> visitor) {
        walkDepthFirstInternal(root, visitor, 0, 0, 0);
    }

    private void walkDepthFirstInternal(
            Node<T> node, NodeVisitor<T> visitor, int depth, int siblings, int orderInSiblings) {
        NodeVisitor.NodeAndPositionInTree nodeAndPositionInTree =
                new NodeVisitor.NodeAndPositionInTree<>(node, depth, siblings, orderInSiblings);
        visitor.enter(nodeAndPositionInTree);
        List<Node<T>> children = new ArrayList<>(node.getChildren());
        if (this.iterationOrderComparator != null) {
            children.sort(this.iterationOrderComparator);
        }
        for (int i = 0; i < children.size(); i++) {
            walkDepthFirstInternal(children.get(i), visitor, depth + 1, children.size() - 1, i);
        }
        visitor.exit(nodeAndPositionInTree);
    }
}
