package io.github.qudtlib.tools.contribute.support.tree;

interface NodeVisitor<T> {
    void enter(Node<T> root);

    void exit(Node<T> root);
}
