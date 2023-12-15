package io.github.qudtlib.tools.contribute.support.tree;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Node<T> {
    private T data;
    private List<Node<T>> children = new ArrayList<>();

    public Node(T data) {
        this.data = data;
    }

    public Node<T> addChild(T child) {
        Node<T> newNode = new Node(child);
        this.children.add(newNode);
        return newNode;
    }

    public Node<T> addChild(Node<T> child) {
        this.children.add(child);
        return child;
    }

    public int size() {
        return this.children.stream().mapToInt(Node::size).sum() + 1;
    }

    @Override
    public String toString() {
        return "[" + data + "" + (children.isEmpty() ? "" : children) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(getData(), node.getData())
                && Objects.equals(getChildren(), node.getChildren());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData(), getChildren());
    }

    public T getData() {
        return data;
    }

    public List<Node<T>> getChildren() {
        return children;
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

    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    public static <T> Optional<Node<T>> insert(
            Node<T> root, Node<T> other, BiPredicate<T, T> isParentOf) {
        if (isParentOf.test(other.getData(), root.getData())) {
            other.addChild(root);
            return Optional.of(other);
        } else if (isParentOf.test(root.getData(), other.getData())) {
            root.addChild(other);
            return Optional.of(root);
        }
        List<Node<T>> newChildren = new ArrayList<>();
        boolean insertDone = false;
        for (Node<T> childNode : root.children) {
            if (!insertDone) {
                Optional<Node<T>> result = insert(childNode, other, isParentOf);
                if (result.isPresent()) {
                    insertDone = true;
                    newChildren.add(result.get());
                } else {
                    newChildren.add(childNode);
                }
            } else {
                newChildren.add(childNode);
            }
        }
        if (insertDone) {
            root.children = newChildren;
            return Optional.of(root);
        }
        return Optional.empty();
    }

    public static <T> List<Node<T>> forestOf(Collection<T> items, BiPredicate<T, T> isParentOf) {
        List<Node<T>> roots = items.stream().map(i -> new Node<>(i)).collect(Collectors.toList());
        int numRoots = Integer.MAX_VALUE;
        boolean finished = false;
        while (!finished) {
            finished = true;
            outer:
            for (int i = 0; i < roots.size() - 1; i++) {
                for (int j = i + 1; j < roots.size(); j++) {
                    Optional<Node<T>> combined =
                            Node.insert(roots.get(i), roots.get(j), isParentOf);
                    if (combined.isPresent()) {
                        roots.remove(j);
                        roots.remove(i);
                        roots.add(combined.get());
                        finished = false;
                        break outer;
                    }
                }
            }
        }
        return roots;
    }

    public static <T> Builder<T> builder(T item) {
        return new Builder<>(item);
    }

    public int getChildrenCount() {
        return this.children == null ? 0 : this.children.size();
    }

    public static class Builder<T> {
        private Node<T> root;
        private ArrayDeque<Node<T>> pathToCurrent = new ArrayDeque<>();

        Builder(T root) {
            this.root = new Node(root);
            pathToCurrent.push(this.root);
        }

        public Builder<T> leaf(T item) {
            this.pathToCurrent.peek().addChild(item);
            return this;
        }

        public Builder<T> inner(T item) {
            Node<T> inner = new Node<>(item);
            this.pathToCurrent.peek().addChild(inner);
            this.pathToCurrent.push(inner);
            return this;
        }

        public Builder<T> up() {
            this.pathToCurrent.pop();
            return this;
        }

        public Node<T> build() {
            return root;
        }
    }
}
