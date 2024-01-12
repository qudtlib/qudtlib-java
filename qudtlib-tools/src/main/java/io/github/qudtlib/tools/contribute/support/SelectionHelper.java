package io.github.qudtlib.tools.contribute.support;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.Unit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectionHelper {
    public static List<Unit> getUnitsAssociatedWithQuantityKind(QuantityKind quantityKind) {
        return Qudt.allUnits().stream()
                .filter(u -> u.getQuantityKinds().contains(quantityKind))
                .collect(Collectors.toList());
    }

    public static Set<QuantityKind> getQuantityKindsByDimensionVector(String dimensionVectorIri) {
        return Qudt.allQuantityKinds().stream()
                .filter(
                        qk ->
                                qk.getDimensionVectorIri()
                                        .map(dv -> dv.equals(dimensionVectorIri))
                                        .orElse(false))
                .collect(Collectors.toSet());
    }

    public static Set<Unit> getUnitsByDimensionVector(String dimensionVectorIri) {
        return Qudt.allUnits().stream()
                .filter(
                        unit ->
                                unit.getDimensionVectorIri()
                                        .map(dv -> dv.equals(dimensionVectorIri))
                                        .orElse(false))
                .collect(Collectors.toSet());
    }

    public static boolean hasOtherUnitsAssociated(QuantityKind quantityKind, Unit except) {
        List<Unit> ret = getUnitsAssociatedWithQuantityKind(quantityKind);
        ret.remove(except);
        return !ret.isEmpty();
    }
}
