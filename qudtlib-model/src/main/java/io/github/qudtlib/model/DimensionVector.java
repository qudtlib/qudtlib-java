package io.github.qudtlib.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Represents the QUDT dimension vector and allows for converting between a dimension vector IRI and
 * the numeric values, as well as for some manipulations.
 *
 * <p>Note that the last value, the 'D' dimension is special: it is only an indicator that the
 * dimension vector represents a ratio (causing all other dimensions to cancel each other out). It
 * never changes by multiplication, and its value is only 1 iff all other dimensions are 0.
 */
public class DimensionVector {

    private static final char[] dimensions = new char[] {'A', 'E', 'L', 'I', 'M', 'H', 'T', 'D'};
    private static final int INDEX_AMOUNT_OF_SUBSTANCE = 0;
    private static final int INDEX_ELECTRIC_CURRENT = 1;
    private static final int INDEX_LENGTH = 2;
    private static final int INDEX_LUMINOUS_INTENSITY = 3;
    private static final int INDEX_MASS = 4;
    private static final int INDEX_TEMPERATURE = 5;
    private static final int INDEX_TIME = 6;

    public static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        FORMAT = new DecimalFormat("0.#", symbols);
    }

    private static String PT = "pt";

    public static DimensionVector DIMENSIONLESS =
            new DimensionVector(new int[] {0, 0, 0, 0, 0, 0, 0, 1});

    private String dimensionVectorIri;

    private final float[] values;

    public static Optional<DimensionVector> of(String dimensionVectorIri) {
        try {
            return Optional.of(new DimensionVector(dimensionVectorIri));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static DimensionVector ofRequired(String dimensionVectorIri) {
        return new DimensionVector(dimensionVectorIri);
    }

    public static Optional<DimensionVector> of(int[] dimensionValues) {
        try {
            return Optional.of(new DimensionVector(dimensionValues));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static DimensionVector ofRequired(int[] dimensionValues) {
        return new DimensionVector(dimensionValues);
    }

    public DimensionVector(String dimensionVectorIri) {
        this.dimensionVectorIri = dimensionVectorIri;
        String localName = dimensionVectorIri.substring(dimensionVectorIri.lastIndexOf("/") + 1);
        float[] dimValues = new float[8];
        String[] numbers = localName.split("[^\\-\\dpt]");
        String[] indicators = localName.split("[^[AELIMHTD]]+");
        if (indicators.length != 8) {
            Logger.getLogger(DimensionVector.class.getName())
                    .warning(
                            String.format(
                                    "Cannot process dimension vector iri %s: unexpected number of dimensions: %d",
                                    dimensionVectorIri, numbers.length));
            Arrays.fill(dimValues, 0);
        } else {
            for (int i = 0; i < indicators.length; i++) {

                if (indicators[i].charAt(0) != dimensions[i]) {
                    throw new RuntimeException(
                            String.format(
                                    "Expected dimension indicator '%s', encountered '%s'",
                                    dimensions[i], indicators[i]));
                }
                dimValues[i] =
                        Float.parseFloat(
                                numbers[i + 1].replace(
                                        "pt", ".")); // split produces an empty first array element
            }
        }

        this.values = dimValues;
    }

    public DimensionVector(int[] dimensionValues) {
        if (dimensionValues.length != 8) {
            throw new RuntimeException(
                    "wrong dimensionality, expected 8, got " + dimensionValues.length);
        }
        StringBuilder sb = new StringBuilder();

        this.values = new float[8];

        for (int i = 0; i < 8; i++) {
            sb.append(dimensions[i]).append(dimensionValues[i]);
            this.values[i] = noNegativeZero((float) dimensionValues[i]);
        }

        this.dimensionVectorIri = QudtNamespaces.dimensionVector.makeIriInNamespace(sb.toString());
    }

    public DimensionVector(float[] dimensionValues) {
        if (dimensionValues.length != 8) {
            throw new RuntimeException(
                    "wrong dimensionality, expected 8, got " + dimensionValues.length);
        }
        StringBuilder sb = new StringBuilder();

        this.values = dimensionValues;

        for (int i = 0; i < 8; i++) {
            this.values[i] = noNegativeZero(values[i]);
            sb.append(dimensions[i]).append(iriFormat(values[i]));
        }

        this.dimensionVectorIri = QudtNamespaces.dimensionVector.makeIriInNamespace(sb.toString());
    }

    private static float noNegativeZero(float f) {
        if (f == -0.0f) {
            return 0.0f;
        }

        return f;
    }

    private static String iriFormat(float dimensionValues) {
        // Note: This handles a weird case where you may have "-0" as a value.
        if (Math.abs(dimensionValues) < 0.01) {
            return "0";
        }

        return FORMAT.format(dimensionValues).replace(".", "pt");
    }

    public DimensionVector() {
        this(new int[8]);
    }

    public boolean isDimensionless() {
        return this.equals(DIMENSIONLESS);
    }

    public String getDimensionVectorIri() {
        return dimensionVectorIri;
    }

    public float[] getValues() {
        return values;
    }

    public float getAmountOfSubstanceExponent() {
        return this.values[INDEX_AMOUNT_OF_SUBSTANCE];
    }

    public float getElectricCurrentExponent() {
        return this.values[INDEX_ELECTRIC_CURRENT];
    }

    public float getLenghExponent() {
        return this.values[INDEX_LENGTH];
    }

    public float getLuminousIntensityExponent() {
        return this.values[INDEX_LUMINOUS_INTENSITY];
    }

    public float getMassExponent() {
        return this.values[INDEX_MASS];
    }

    public float getTemperatureExponent() {
        return this.values[INDEX_TEMPERATURE];
    }

    public float getTimeExponent() {
        return this.values[INDEX_TIME];
    }

    public DimensionVector multiply(float by) {
        float[] mult = new float[8];
        boolean isRatio = true;
        for (int i = 0; i < 7; i++) {
            mult[i] = this.values[i] * by;
            if (mult[i] != 0) {
                isRatio = false;
            }
        }
        setRatio(mult, isRatio);
        return new DimensionVector(mult);
    }

    private void setRatio(float[] values, boolean isRatio) {
        values[7] = isRatio ? 1 : 0;
    }

    public DimensionVector combine(DimensionVector other) {
        float[] combined = new float[8];
        boolean isRatio = true;
        for (int i = 0; i < 7; i++) {
            combined[i] = this.values[i] + other.getValues()[i];
            if (combined[i] != 0) {
                isRatio = false;
            }
        }
        setRatio(combined, isRatio);
        return new DimensionVector(combined);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DimensionVector)) return false;
        DimensionVector that = (DimensionVector) o;
        return Objects.equals(getDimensionVectorIri(), that.getDimensionVectorIri())
                && Arrays.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getDimensionVectorIri());
        result = 31 * result + Arrays.hashCode(getValues());
        return result;
    }
}
