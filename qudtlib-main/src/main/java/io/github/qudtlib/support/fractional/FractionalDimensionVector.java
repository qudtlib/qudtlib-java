package io.github.qudtlib.support.fractional;

import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FractionalDimensionVector {
    private String numerator;
    private String denominator;

    public FractionalDimensionVector(String numerator, String denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public FractionalDimensionVector(FactorUnits numerator, FactorUnits denominator) {
        this.numerator = numerator.getDimensionVectorIri();
        this.denominator = denominator.getDimensionVectorIri();
    }

    public FractionalDimensionVector(List<FactorUnit> numerator, List<FactorUnit> denominator) {
        this.numerator = new FactorUnits(numerator).getDimensionVectorIri();
        this.denominator = new FactorUnits(denominator).getDimensionVectorIri();
    }

    public static FractionalDimensionVector onlyNumerator(String dimensionVector) {
        return new FractionalDimensionVector(dimensionVector, null);
    }

    public static FractionalDimensionVector onlyDenominator(String dimensionVector) {
        return new FractionalDimensionVector(null, dimensionVector);
    }

    public Optional<String> getNumerator() {
        return Optional.ofNullable(numerator);
    }

    public Optional<String> getDenominator() {
        return Optional.ofNullable(denominator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FractionalDimensionVector)) return false;
        FractionalDimensionVector that = (FractionalDimensionVector) o;
        return Objects.equals(getNumerator(), that.getNumerator())
                && Objects.equals(getDenominator(), that.getDenominator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumerator(), getDenominator());
    }
}
