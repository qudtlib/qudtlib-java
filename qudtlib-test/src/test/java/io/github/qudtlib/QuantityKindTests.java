package io.github.qudtlib;

import static io.github.qudtlib.model.QuantityKinds.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qudtlib.model.QuantityKind;
import java.lang.reflect.Array;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.Preconditions;

public class QuantityKindTests {
    @MethodSource
    @ParameterizedTest
    public void testExactMatches(
            @AggregateWith(VarargsAggregator.class) QuantityKind... quantityKinds) {
        for (int i = 0; i < quantityKinds.length; i++) {
            for (int j = i + 1; j < quantityKinds.length; j++) {
                QuantityKind qk1 = quantityKinds[i];
                QuantityKind qk2 = quantityKinds[j];
                if (i != j) {
                    assertTrue(
                            qk1.getExactMatches().contains(qk2),
                            String.format(
                                    "(%s).getExactMatches().contains(%s)",
                                    Qudt.NAMESPACES.quantityKind.abbreviate(qk1.getIri()),
                                    Qudt.NAMESPACES.quantityKind.abbreviate(qk2.getIri())));
                    assertTrue(
                            qk2.getExactMatches().contains(qk1),
                            String.format(
                                    "(%s).getExactMatches().contains(%s)",
                                    Qudt.NAMESPACES.quantityKind.abbreviate(qk2.getIri()),
                                    Qudt.NAMESPACES.quantityKind.abbreviate(qk1.getIri())));
                }
            }
        }
    }

    public static Stream<Arguments> testExactMatches() {
        return Stream.of(
                Arguments.of(Velocity, LinearVelocity),
                Arguments.of(PlaneAngle, Angle),
                Arguments.of(AngularImpulse, AngularMomentum),
                Arguments.of(Density, MassDensity),
                Arguments.of(ElectricFluxDensity, ElectricDisplacement),
                Arguments.of(
                        ElectricPotential,
                        Voltage,
                        ElectricPotentialDifference,
                        EnergyPerElectricCharge),
                Arguments.of(ElectromagneticEnergyDensity, VolumicElectromagneticEnergy),
                Arguments.of(Permeability, ElectromagneticPermeability),
                Arguments.of(EnergyInternal, InternalEnergy, ThermodynamicEnergy),
                Arguments.of(InversePressure, IsothermalCompressibility));
    }

    static class VarargsAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
                throws ArgumentsAggregationException {
            Class<?> parameterType = context.getParameter().getType();
            Preconditions.condition(
                    parameterType.isArray(),
                    () -> "must be an array type, but was " + parameterType);
            Class<?> componentType = parameterType.getComponentType();
            return IntStream.range(context.getIndex(), accessor.size())
                    .mapToObj(index -> accessor.get(index, componentType))
                    .toArray(size -> (Object[]) Array.newInstance(componentType, size));
        }
    }
}
