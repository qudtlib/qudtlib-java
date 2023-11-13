package io.github.qudtlib.model;

public class DimensionVector {
    private String dimensionVectorIri;

    private static final char[] dimensions = new char[] {'A', 'E', 'L', 'I', 'M', 'H', 'T', 'D'};

    private final int[] values;

    public static DimensionVector of(String dimensionVectorIri) {
        return new DimensionVector(dimensionVectorIri);
    }

    public static DimensionVector of(int[] dimensionValues) {
        return new DimensionVector(dimensionValues);
    }

    public DimensionVector(String dimensionVectorIri) {
        this.dimensionVectorIri = dimensionVectorIri;
        String localName = dimensionVectorIri.substring(dimensionVectorIri.lastIndexOf("/") + 1);
        int[] dimValues = new int[8];
        String[] numbers = localName.split("[^\\-\\d]");
        String[] indicators = localName.split("-?\\d{1,2}");
        if (indicators.length != 8) {
            throw new RuntimeException(
                    String.format(
                            "Cannot process dimension vector iri %s: unexpected number of dimensions: %d",
                            dimensionVectorIri, numbers.length));
        }
        for (int i = 0; i < indicators.length; i++) {

            if (indicators[i].charAt(0) != dimensions[i]) {
                throw new RuntimeException(
                        String.format(
                                "Expected dimension indicator '%s', encountered '%s'",
                                dimensions[i], indicators[i]));
            }
            dimValues[i] =
                    Integer.parseInt(numbers[i + 1]); // split produces an empty first array element
        }
        this.values = dimValues;
    }

    public DimensionVector(int[] dimensionValues) {
        if (dimensionValues.length != 8) {
            throw new RuntimeException(
                    "wrong dimensionality, expected 8, got " + dimensionValues.length);
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            sb.append(dimensions[i]).append(dimensionValues[i]);
        }
        this.values = dimensionValues;
        this.dimensionVectorIri = "http://qudt.org/vocab/dimensionvector/" + sb.toString();
    }

    public DimensionVector() {
        this(new int[8]);
    }

    public String getDimensionVectorIri() {
        return dimensionVectorIri;
    }

    public int[] getValues() {
        return values;
    }

    public DimensionVector multiply(int by) {
        int[] mult = new int[8];
        for (int i = 0; i < 8; i++) {
            mult[i] = this.values[i] * by;
        }
        return new DimensionVector(mult);
    }

    public DimensionVector combine(DimensionVector other) {
        int[] combined = new int[8];
        for (int i = 0; i < 8; i++) {
            combined[i] = this.values[i] + other.getValues()[i];
        }
        return new DimensionVector(combined);
    }
}
