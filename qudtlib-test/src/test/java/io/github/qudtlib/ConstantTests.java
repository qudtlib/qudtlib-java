package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.*;

import io.github.qudtlib.model.PhysicalConstant;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class ConstantTests {
    @Test
    public void vanillaConstantTest() {
        PhysicalConstant g =
                Qudt.physicalConstantRequired(
                        Qudt.NAMESPACES.constant.makeIriInNamespace(
                                "StandardAccelerationOfGravity"));
        assertEquals(Qudt.QuantityKinds.LinearAcceleration, g.getQuantityKind());
        MatcherAssert.assertThat(
                new BigDecimal("9.80665"),
                Matchers.comparesEqualTo(g.getConstantValue().getValue()));
    }

    public void descriptionTest() {
        assertTrue(Qudt.PhysicalConstants.AvogadroConstant.getDescription().isPresent());
    }
}
