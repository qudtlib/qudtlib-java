package io.github.qudtlib.tools.contribute.support.tree;

public interface NodeVisitor<T> {
    void enter(NodeAndPositionInTree<T> nodeAndPosition);

    void exit(NodeAndPositionInTree<T> nodeAndPosition);

    static class NodeAndPositionInTree<T> {
        private final Node<T> node;
        private final int depth;
        private final int siblings;
        private final int orderInSiblings;

        public NodeAndPositionInTree(Node<T> node, int depth, int siblings, int orderInSiblings) {
            this.node = node;
            this.depth = depth;
            this.siblings = siblings;
            this.orderInSiblings = orderInSiblings;
        }

        public Node<T> getNode() {
            return node;
        }

        public int getDepth() {
            return depth;
        }

        public int getSiblings() {
            return siblings;
        }

        public int getOrderInSiblings() {
            return orderInSiblings;
        }

        public boolean isFirstOrOnlyNode() {
            return orderInSiblings == 0;
        }

        public boolean isLastOrOnlyNode() {
            return orderInSiblings == siblings;
        }

        public boolean isNeitherFirstNorLast() {
            return orderInSiblings > 0 && orderInSiblings < siblings;
        }
    }
}
