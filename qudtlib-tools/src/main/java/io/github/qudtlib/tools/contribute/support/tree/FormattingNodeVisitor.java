package io.github.qudtlib.tools.contribute.support.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class FormattingNodeVisitor<T> implements NodeVisitor<T> {
    private final StringBuilder sb;
    int indent = 0;

    BiConsumer<StringBuilder, Node<T>> nodeFormatter;
    Function<Node<T>,String> nodeIndentProvider;
    private int indentSize = 4;

    public FormattingNodeVisitor(
            StringBuilder sb, BiConsumer<StringBuilder, Node<T>> nodeFormatter, Function<Node<T>, String> nodeIndentProvider) {
        this.sb = sb;
        this.nodeFormatter = nodeFormatter;
        this.nodeIndentProvider = nodeIndentProvider;
    }

    public FormattingNodeVisitor(
                    StringBuilder sb, BiConsumer<StringBuilder, Node<T>> nodeFormatter) {
        this(sb, nodeFormatter, n -> "--");
    }



    @Override
    public void enter(Node<T> node) {
        IntStream.range(0,this.indent).forEach(i -> sb.append(' '));
        sb.append("+").append(this.nodeIndentProvider.apply(node));
        formatNodeData(node);
        sb.append("\n");
        this.indent += this.indentSize;
    }


    private void formatNodeData(Node<T> node) {
        this.nodeFormatter.accept(this.sb, node);
    }

    @Override
    public void exit(Node<T> node) {
        this.indent -= this.indentSize;
    }
}
