package io.github.qudtlib.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class BigDec {

    public static final BigDecimal ONE_THOUSANDTH = new BigDecimal("0.001");
    public static final BigDecimal ONE_MILLIONTH = new BigDecimal("0.000001");

    public static boolean isRelativeDifferenceGreaterThan(
            BigDecimal left, BigDecimal right, BigDecimal epsilon) {
        return greaterThan(relativeValueDifference(left, right), epsilon);
    }

    /**
     * Returns the difference between the two values in relation to the value of their mean
     *
     * @return
     */
    static BigDecimal relativeValueDifference(BigDecimal left, BigDecimal right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        BigDecimal mean =
                left.add(right)
                        .divide(BigDecimal.valueOf(2), MathContext.DECIMAL128)
                        .abs(MathContext.DECIMAL128);
        BigDecimal diff =
                left.abs(MathContext.DECIMAL128)
                        .subtract(right.abs(MathContext.DECIMAL128))
                        .abs(MathContext.DECIMAL128);
        return diff.divide(mean, MathContext.DECIMAL128).abs();
    }

    static boolean greaterThan(BigDecimal left, BigDecimal right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        return left.subtract(right).signum() > 0;
    }
}
