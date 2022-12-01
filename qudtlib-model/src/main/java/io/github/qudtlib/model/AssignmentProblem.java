package io.github.qudtlib.model;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class AssignmentProblem {
    /**
     * A problem instance, defined by its weight matrix. The matrix may not have more rows than
     * columns. A valid solution is the assigment of one column per row, such that no columns are
     * selected more than once and the sum of the assignment is minimal. Only one such solution is
     * generated.
     */
    public abstract static class Instance {
        protected final double[][] weights;
        protected final int rows;
        protected final int cols;

        public Instance(double[][] weights) {
            this.weights = weights;
            this.rows = weights.length;
            this.cols = weights[0].length;
        }

        public double[][] getWeights() {
            return weights;
        }

        public Double weightOfAssignment(int[] selection) {
            if (selection == null || selection.length == 0) {
                return null;
            }
            double sum = 0;
            for (int i = 0; i < selection.length; i++) {
                sum += weights[i][selection[i]];
            }
            return sum;
        }

        public abstract Solution solve();
    }

    public static class NaiveAlgorithmInstance extends Instance {
        private Solution currentBestSolution;

        public NaiveAlgorithmInstance(double[][] weights) {
            super(weights);
        }

        public boolean isLowerThanBestWeight(double weightToTest) {
            if (currentBestSolution == null) {
                return true;
            }
            if (!currentBestSolution.isComplete()) {
                return true;
            }
            return currentBestSolution.weight > weightToTest;
        }

        public void updateBestSolutionIfPossible(Solution candidate) {
            if (currentBestSolution == null
                    || candidate.isBetterSolutionThan(currentBestSolution)) {
                this.currentBestSolution = candidate;
            }
        }

        @Override
        public Solution solve() {
            solve(0, new Solution(this));
            return this.currentBestSolution;
        }

        private void solve(int row, Solution solution) {
            if (row >= this.rows) {
                updateBestSolutionIfPossible(solution);
                return;
            }
            if (!solution.isEmpty()) {
                double bestAttainableScore = sum(minPerRow(row, solution.assignment));
                if (!isLowerThanBestWeight(solution.weight + bestAttainableScore)) {
                    return;
                }
            }
            ValueWithIndex[] nMin = rowSortedAscending(this.weights, row, solution.getAssignment());
            for (int i = 0; i < nMin.length; i++) {
                if ((solution.isEmpty())
                        || isLowerThanBestWeight(solution.getWeight() + nMin[i].value)) {
                    solve(row + 1, solution.assignColumnInNextRow(nMin[i].index));
                }
            }
        }

        private double[] minPerRow(int startRow, int[] skipCols) {
            double[] ret = new double[weights.length - startRow];
            for (int r = startRow; r < weights.length; r++) {
                double min = Double.MAX_VALUE;
                for (int c = 0; c < weights[r].length; c++) {
                    if (!containsValue(skipCols, c)) {
                        if (min > weights[r][c]) {
                            min = weights[r][c];
                        }
                    }
                }
                ret[r - startRow] = min;
            }
            return ret;
        }

        private double sum(double[] arr) {
            return DoubleStream.of(arr).sum();
        }
    }

    public static class Solution {
        /**
         * for each row in the instance, contains the index of the column assigned for this solution
         * *
         */
        private final int[] assignment;
        /** sum of the assigned weights * */
        private final Double weight;

        private final Instance instance;

        public Solution(Instance instance) {
            this(instance, new int[0]);
        }

        public Solution(Instance instance, int[] assignment) {
            this.assignment = assignment;
            this.instance = instance;
            this.weight = instance.weightOfAssignment(assignment);
        }

        public int[] getAssignment() {
            return assignment;
        }

        public Double getWeight() {
            return weight;
        }

        public Solution assignColumnInNextRow(int col) {
            if (isComplete()) {
                throw new IllegalStateException("Solution is already complete");
            }
            return new Solution(this.instance, append(this.assignment, col));
        }

        private boolean isComplete() {
            return this.assignment.length >= instance.rows;
        }

        public boolean isEmpty() {
            return this.assignment == null || this.assignment.length == 0;
        }

        public boolean isBetterSolutionThan(Solution other) {
            if (!(this.isComplete() && other.isComplete())) {
                throw new IllegalStateException("Cannot compare incomplete solutions");
            }
            return this.weight < other.weight;
        }
    }

    public static Instance instance(double[][] weights) {
        int rows = weights.length;
        if (rows == 0) {
            throw new IllegalArgumentException("Cannot create instance with 0x0 weights matrix");
        }
        int cols = weights[0].length;
        if (rows > cols) {
            throw new IllegalArgumentException(
                    "The weights matrix may not have more rows than columns");
        }
        return new NaiveAlgorithmInstance(copy(weights));
    }

    private static double[][] copy(double[][] weights) {
        int rows = weights.length;
        int cols = weights[0].length;
        double[][] ret = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(weights[i], 0, ret[i], 0, cols);
        }
        return ret;
    }

    private static double[][] flipMatrix(double[][] mat) {
        int rows = mat.length;
        int cols = mat[0].length;
        double[][] ret = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ret[j][i] = mat[i][j];
            }
        }
        return ret;
    }

    private static int[] append(int[] arr, int val) {
        int[] ret = new int[arr.length + 1];
        System.arraycopy(arr, 0, ret, 0, arr.length);
        ret[ret.length - 1] = val;
        return ret;
    }

    private static class ValueWithIndex {
        private final int index;
        private final double value;

        public ValueWithIndex(double value, int index) {
            this.index = index;
            this.value = value;
        }
    }

    private static ValueWithIndex[] rowSortedAscending(
            double[][] weights, int row, int[] skipCols) {
        int cols = weights[row].length;
        ValueWithIndex[] sorted = new ValueWithIndex[cols - skipCols.length];
        int j = 0;
        for (int i = 0; i < cols; i++) {
            if (!containsValue(skipCols, i)) {
                sorted[j++] = new ValueWithIndex(weights[row][i], i);
            }
        }
        Arrays.sort(sorted, (left, right) -> (int) Math.signum(left.value - right.value));
        return sorted;
    }

    private static ValueWithIndex[] rowNMin(double[][] mat, int row, int n, int[] skipCols) {
        ValueWithIndex[] nMin = new ValueWithIndex[n];
        for (int i = 0; i < mat[0].length; i++) {
            if (!containsValue(skipCols, i)) {
                replaceIfLower(nMin, mat[row][i], i);
            }
        }
        Arrays.sort(nMin, (left, right) -> (int) Math.signum(left.value - right.value));
        return nMin;
    }

    private static boolean containsValue(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == val) return true;
        }
        return false;
    }

    private static void replaceIfLower(ValueWithIndex[] nMin, double value, int index) {
        int maxValueIndex = -1;
        double maxValue = -Double.MAX_VALUE;
        for (int i = 0; i < nMin.length; i++) {
            if (nMin[i] == null) {
                nMin[i] = new ValueWithIndex(value, index);
                return;
            }
            if (nMin[i].value > maxValue) {
                maxValue = nMin[i].value;
                maxValueIndex = i;
            }
        }
        if (maxValue > value) {
            nMin[maxValueIndex] = new ValueWithIndex(value, index);
        }
    }

    private static ValueWithIndex rowMax(double[][] mat, int row, int[] skipCols) {
        if (skipCols == null) {
            skipCols = new int[] {-1};
        }
        Arrays.sort(skipCols);
        double max = -Double.MAX_VALUE;
        int index = -1;
        int cols = mat[0].length;
        for (int i = 0; i < cols; i++) {
            if (Arrays.binarySearch(skipCols, i) > -1) {
                continue;
            }
            double cur = mat[row][i];
            if (cur > max) {
                index = i;
                max = cur;
            }
        }
        return new ValueWithIndex(max, index);
    }

    private static ValueWithIndex colMax(double[][] mat, int col, int[] skipRows) {
        if (skipRows == null) {
            skipRows = new int[] {-1};
        }
        Arrays.sort(skipRows);
        double max = -Double.MAX_VALUE;
        int index = -1;
        int rows = mat.length;
        for (int i = 0; i < rows; i++) {
            if (Arrays.binarySearch(skipRows, i) > -1) {
                continue;
            }
            double cur = mat[i][col];
            if (cur > max) {
                index = i;
                max = cur;
            }
        }
        return new ValueWithIndex(max, index);
    }

    private static double[][] copyWithoutCol(double[][] mat, int col) {
        int rows = mat.length;
        int cols = mat[0].length;
        double[][] ret = new double[rows][cols - 1];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 1; c++) {
                int readCol = c >= col ? c + 1 : c;
                ret[r][c] = mat[r][readCol];
            }
        }
        return ret;
    }
}
