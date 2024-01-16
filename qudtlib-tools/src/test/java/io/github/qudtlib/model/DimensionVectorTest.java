package io.github.qudtlib.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DimensionVectorTest {

    @Test
    void getDimensionVectorIri() {
        DimensionVector dimensionVector =
                new DimensionVector(new float[] {0f, 1f, -0f, 0.5f, -1, 2.5f, 0, 0});

        String iri = dimensionVector.getDimensionVectorIri();

        Assertions.assertEquals(
                "http://qudt.org/vocab/dimensionvector/A0E1L0I0pt5M-1H2pt5T0D0", iri);

        DimensionVector fromString = new DimensionVector(iri);

        Assertions.assertEquals(dimensionVector, fromString);
    }
}
