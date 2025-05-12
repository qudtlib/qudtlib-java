package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.util.*;
import java.util.stream.Collectors;

public class GlobalData {
    final Map<Unit, Set<Unit>> missingData = new HashMap<>();

    final Map<Unit, Set<Unit>> unitDependencies;
    final String relativeDifferenceThreshold = "0.001";
    final Set<Unit> correctUnits = new HashSet<>();

    final Set<Unit> wasIncorrect = new HashSet<>();
    final Set<Unit> wasMissing = new HashSet<>();

    public GlobalData() {
        this.unitDependencies = calculateUnitDependencies();
    }

    public boolean trustCalculationForUnit(Unit u) {
        Set<Unit> dependencies = unitDependencies.get(u);
        if (dependencies == null) {
            return false;
        }
        if (dependencies.equals(Set.of(u))) {
            return true;
        }
        return dependencies.stream().allMatch(dependency -> correctUnits.contains(dependency));
    }

    private static Map<Unit, Set<Unit>> calculateUnitDependencies() {
        return Qudt.allUnits().stream()
                .collect(
                        Collectors.toMap(
                                u -> u,
                                u ->
                                        u.getFactorUnits()
                                                .streamAllFactorUnitsRecursively()
                                                .map(FactorUnit::getUnit)
                                                .collect(Collectors.toSet())));
    }
}
