package io.github.qudtlib.tools.contribute.support;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ContributionHelper {
    public static Unit.Definition derivedUnitDefinition(FactorUnits factorUnits) {
        String localname = factorUnits.getLocalname();
        Unit.Definition def = null;
        Unit existingUnit = findUnitByLocalName(factorUnits);
        if (existingUnit == null) {
            def = Unit.definition(QudtNamespaces.unit.makeIriInNamespace(localname));
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
                                    "Cannot create unit %s: the factor units %s define a scaled unit, but there is no QUDT unit for the base factor units %s. Add a unit for these factors first.",
                                    factorUnits.getLocalname(),
                                    factorUnits.toString(),
                                    base.getFactorUnits().toString()));
                }
                Unit scalingOf = units.stream().findFirst().get();
                def.conversionMultiplier(factorUnits.conversionFactor(scalingOf));
                def.scalingOf(scalingOf);
            }
            def.addFactorUnits(factorUnits).dimensionVectorIri(factorUnits.getDimensionVectorIri());
        } else {
            def = Unit.definition(existingUnit.getIri());
            final Unit.Definition finalDef = def;
            existingUnit.getFactorUnits().forEach(fu -> finalDef.addFactorUnit(fu));
            FactorUnits base = getBaseFactorUnits(factorUnits);
            if (existingUnit.getScalingOf().isPresent()) {
                finalDef.scalingOf(existingUnit.getScalingOf().get());
                existingUnit
                        .getConversionMultiplier()
                        .ifPresent(c -> finalDef.conversionMultiplier(c));
                existingUnit.getConversionOffset().ifPresent(co -> finalDef.conversionOffset(co));
            } else {
                Set<Unit> baseUnits =
                        Qudt.derivedUnitsFromFactorUnits(
                                DerivedUnitSearchMode.BEST_MATCH, base.getFactorUnits());
                Optional<Unit> newBase = baseUnits.stream().findFirst();
                if (newBase.isEmpty() || newBase.get().equals(existingUnit)) {
                    finalDef.conversionMultiplier(BigDecimal.ONE);
                } else {
                    finalDef.scalingOf(newBase.get());
                    finalDef.conversionMultiplier(factorUnits.conversionFactor(newBase.get()));
                }
            }
            existingUnit.getUcumCode().ifPresent(ucum -> finalDef.ucumCode(ucum));

            def.setFactorUnits(factorUnits).dimensionVectorIri(factorUnits.getDimensionVectorIri());
        }
        def.symbol(factorUnits.getSymbol().orElse(null))
                .ucumCode(factorUnits.getUcumCode().orElse(null))
                .addLabels(LabelCombiner.forFactorUnits(factorUnits));
        return def;
    }

    private static Unit findUnitByLocalName(FactorUnits factorUnits) {
        List<String> possibilities = factorUnits.generateAllLocalnamePossibilities();
        for (String localnameCandidate : possibilities) {
            Optional<Unit> existingUnit = Qudt.unitFromLocalname(localnameCandidate);
            if (existingUnit.isPresent()) {
                return existingUnit.get();
            } else {
                existingUnit = Qudt.currencyFromLocalname(localnameCandidate);
                if (existingUnit.isPresent()) {
                    return existingUnit.get();
                }
            }
        }
        return null;
    }

    /** Retuns the FactorUnits object resulting from unscaling each factor unit. */
    static FactorUnits getBaseFactorUnits(FactorUnits factorUnits) {
        return new FactorUnits(
                factorUnits.getFactorUnits().stream()
                        .map(
                                fu ->
                                        new FactorUnit(
                                                Qudt.unscale(fu.getUnit(), true, false),
                                                fu.getExponent()))
                        .collect(Collectors.toList()));
    }
}
