package io.github.qudtlib.tools.contribute.support.tree;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Node<T> {
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

    public Node<T> addChild(Node<T> child) {
        this.children.add(child);
        return child;
    }

    public T getData() {
        return data;
    }

    public Set<Node<T>> getChildren() {
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
}
