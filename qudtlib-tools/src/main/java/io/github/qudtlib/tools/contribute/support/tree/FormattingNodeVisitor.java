package io.github.qudtlib.tools.contribute.support.tree;

import java.util.function.BiConsumer;

public class FormattingNodeVisitor<T> implements NodeVisitor<T> {
    private final StringBuilder sb;
    String indent;
    int indentSize;
    String currentIndent;

    BiConsumer<StringBuilder, Node<T>> nodeFormatter;

    public FormattingNodeVisitor(
            StringBuilder sb, BiConsumer<StringBuilder, Node<T>> nodeFormatter) {
        this.sb = sb;
        indent = "|   ";
        indentSize = indent.length();
        currentIndent = "    +-- ";
        this.nodeFormatter = nodeFormatter;
    }

    @Override
    public void enter(Node<T> node) {
        sb.append(currentIndent);
        formatNodeData(node);
        sb.append("\n");
        currentIndent =
                currentIndent.substring(0, indentSize)
                        + indent
                        + currentIndent.substring(indentSize);
    }

    private void formatNodeData(Node<T> node) {
        this.nodeFormatter.accept(this.sb, node);
    }

    @Override
    public void exit(Node<T> node) {
        currentIndent =
                currentIndent.substring(0, indentSize)
                        + currentIndent.substring(indentSize + indentSize);
    }
}
