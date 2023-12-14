package io.github.qudtlib.tools.contribute.support.tree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class FormattingNodeVisitor<T> implements NodeVisitor<T> {
    private final StringBuilder sb;
    Deque<TreeDrawingConfig> treeDrawingConfigDeque = new ArrayDeque<>();
    NodeFormatter nodeFormatter;
    private int indentSize = 4;
    private BiConsumer<TreeDrawingConfig, NodeAndPositionInTree<T>> treeDrawingConfigurer =
            (config, pos) -> {};

    public static interface NodeToString<T> extends Function<Node<T>, String> {}
    ;

    public static interface NodeToOptString<T> extends Function<Node<T>, Optional<String>> {}
    ;

    public static class TreeDrawingConfig {
        Character trunk;
        Character branchStart;

        Character branchStartLast;
        Character branch;

        Character branchEndLeaf;
        Character branchEndInner;

        int indentSize = 4;

        public TreeDrawingConfig(
                Character trunk,
                Character branchStart,
                Character branchStartLast,
                Character branch,
                Character branchEndLeaf,
                Character branchEndInner,
                int indentSize) {
            this.trunk = trunk;
            this.branchStart = branchStart;
            this.branchStartLast = branchStartLast;
            this.branch = branch;
            this.branchEndLeaf = branchEndLeaf;
            this.branchEndInner = branchEndInner;
            this.indentSize = indentSize;
        }

        public static TreeDrawingConfig standard() {
            return new TreeDrawingConfig(
                    CHAR_SLIM_VERT,
                    CHAR_SLIM_SLIM_RIGHT_T,
                    CHAR_SLIM_SLIM_ANGLE,
                    CHAR_SLIM_HORIZ,
                    CHAR_SLIM_HORIZ,
                    CHAR_SLIM_SLIM_T,
                    4);
        }

        public TreeDrawingConfig trunk(Character c) {
            this.trunk = c;
            return this;
        }

        public TreeDrawingConfig branchStart(Character c) {
            this.branchStart = c;
            return this;
        }

        public TreeDrawingConfig branchStartLast(Character c) {
            this.branchStartLast = c;
            return this;
        }

        public TreeDrawingConfig branch(Character c) {
            this.branch = c;
            return this;
        }

        public TreeDrawingConfig branchEndLeaf(Character c) {
            this.branchEndLeaf = c;
            return this;
        }

        public TreeDrawingConfig branchEndInner(Character c) {
            this.branchEndInner = c;
            return this;
        }

        public TreeDrawingConfig indentSize(int indentSize) {
            this.indentSize = indentSize;
            return this;
        }

        public Character getTrunk() {
            return trunk;
        }

        public Character getBranchStart() {
            return branchStart;
        }

        public Character getBranchStartLast() {
            return branchStartLast;
        }

        public Character getBranch() {
            return branch;
        }

        public Character getBranchEndLeaf() {
            return branchEndLeaf;
        }

        public Character getBranchEndInner() {
            return branchEndInner;
        }

        public int getIndentSize() {
            return this.indentSize;
        }
    }

    private class NodeFormatter implements NodeToString<T> {
        private NodeToString<T> fallback;
        private List<NodeToOptString<T>> providers = new ArrayList<>();

        public NodeFormatter(NodeToString<T> fallback) {
            this.fallback = fallback;
        }

        public NodeFormatter setFormatDefault(NodeToString<T> fallback) {
            this.fallback = fallback;
            return this;
        }

        public NodeFormatter addFormatOpt(NodeToOptString<T> indentOpt) {
            this.providers.add(indentOpt);
            return this;
        }

        @Override
        public String apply(Node<T> node) {
            Optional<String> formattedOpt =
                    providers.stream()
                            .map(p -> p.apply(node))
                            .filter(o -> o.isPresent())
                            .map(o -> o.orElse(null))
                            .findFirst();
            return formattedOpt.orElseGet(() -> this.fallback.apply(node));
        }
    }

    public FormattingNodeVisitor() {
        this(new StringBuilder());
    }

    public static final Character CHAR_SLIM_SLIM_ANGLE = '└';
    public static final Character CHAR_SLIM_SLIM_ROUNDED = '╰';
    public static final Character CHAR_SLIM_DOUBLE_ANGLE = '╘';
    public static final Character CHAR_SLIM_HORIZ = '─';
    public static final Character CHAR_SLIM_SLIM_T = '┬';
    public static final Character CHAR_DOUBLE_SLIM_T = '╤';
    public static final Character CHAR_SLIM_SLIM_RIGHT_T = '├';
    public static final Character CHAR_SLIM_DOUBLE_RIGHT_T = '╞';
    public static final Character CHAR_SLIM_VERT = '│';
    public static final Character CHAR_DOUBLE_HORIZ = '═';
    public static final Character CHAR_NOTHING = ' ';

    public FormattingNodeVisitor(StringBuilder sb) {
        this.sb = sb;
        this.nodeFormatter = new NodeFormatter(n -> n.getData().toString());
    }

    public FormattingNodeVisitor nodeFormatDefault(NodeToString<T> defaultFormat) {
        this.nodeFormatter.setFormatDefault(defaultFormat);
        return this;
    }

    public FormattingNodeVisitor nodeFormatOpt(NodeToOptString<T> optFormat) {
        this.nodeFormatter.addFormatOpt(optFormat);
        return this;
    }

    public FormattingNodeVisitor treeDrawingConfig(
            BiConsumer<TreeDrawingConfig, NodeAndPositionInTree<T>> treeDrawingConfigurer) {
        this.treeDrawingConfigurer = treeDrawingConfigurer;
        return this;
    }

    @Override
    public void enter(NodeAndPositionInTree<T> nodeAndPosition) {
        TreeDrawingConfig config = TreeDrawingConfig.standard();
        this.treeDrawingConfigurer.accept(config, nodeAndPosition);
        if (nodeAndPosition.isLastOrOnlyNode()) {
            config.trunk(CHAR_NOTHING);
        }
        if (nodeAndPosition.getDepth() == 0) {
            config.branchStart(CHAR_NOTHING);
            config.branchStartLast(CHAR_NOTHING);
        }
        drawTrunks();
        this.treeDrawingConfigDeque.push(config);
        drawBranch(nodeAndPosition.getNode(), nodeAndPosition.isLastOrOnlyNode());
    }

    private void drawTrunks() {
        Iterator<TreeDrawingConfig> it = treeDrawingConfigDeque.descendingIterator();
        while (it.hasNext()) {
            TreeDrawingConfig config = it.next();
            sb.append(config.getTrunk());
            IntStream.range(1, config.getIndentSize() - 1).forEach(i -> sb.append(' '));
        }
    }

    private void drawBranch(Node<T> node, boolean isLastChild) {
        TreeDrawingConfig config = this.treeDrawingConfigDeque.peek();
        if (isLastChild) {
            sb.append(config.getBranchStartLast());
        } else {
            sb.append(config.getBranchStart());
        }
        IntStream.range(1, config.getIndentSize() - 1).forEach(i -> sb.append(config.getBranch()));
        if (node.hasChildren()) {
            sb.append(config.getBranchEndInner());
        } else {
            sb.append(config.getBranchEndLeaf());
        }
        formatNodeData(node);
        sb.append("\n");
    }

    private void formatNodeData(Node<T> node) {
        sb.append(" ").append(this.nodeFormatter.apply(node));
    }

    @Override
    public void exit(NodeAndPositionInTree<T> nodeAndPosition) {
        this.treeDrawingConfigDeque.pop();
    }
}
