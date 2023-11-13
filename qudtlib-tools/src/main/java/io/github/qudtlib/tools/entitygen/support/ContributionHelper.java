package io.github.qudtlib.tools.entitygen.support;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class ContributionHelper {
    public static Unit.Definition derivedUnitDefinition(FactorUnits factorUnits) {
        Unit.Definition def =
                Unit.definition(QudtNamespaces.unit.makeIriInNamespace(factorUnits.getLocalname()));
        FactorUnits base = getBaseFactorUnits(factorUnits);
        if (base.equals(factorUnits)) {
            def.conversionMultiplier(BigDecimal.ONE);
        } else {
            Set<Unit> units =
                    Qudt.derivedUnitsFromFactorUnits(
                            DerivedUnitSearchMode.BEST_MATCH, base.getFactorUnits());
            if (units.isEmpty()) {
                throw new RuntimeException(
                        String.format(
                                "The factor units %s define a scaled unit, but there is no QUDT unit for the base factor units %s. Add a unit for these factors first.",
                                factorUnits.toString(), base.getFactorUnits().toString()));
            }
            Unit scalingOf = units.stream().findFirst().get();
            def.conversionMultiplier(factorUnits.conversionFactor(scalingOf));
            def.scalingOf(scalingOf);
        }
        def.addFactorUnits(factorUnits)
                .dimensionVectorIri(factorUnits.getDimensionVectorIri())
                .symbol(factorUnits.getSymbol())
                .addLabels(LabelCombiner.forFactorUnits(factorUnits));
        return def;
    }

    /** Retuns the FactorUnits object resulting from unscaling each factor unit. */
    static FactorUnits getBaseFactorUnits(FactorUnits factorUnits) {
        return new FactorUnits(
                factorUnits.getFactorUnits().stream()
                        .map(fu -> new FactorUnit(Qudt.unscale(fu.getUnit()), fu.getExponent()))
                        .collect(Collectors.toList()));
    }
}
