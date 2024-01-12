package io.github.qudtlib.tools.contribute.support;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ContributionHelper {
    public static Unit.Definition derivedUnitDefinition(
            FactorUnits factorUnits, String nonstandardLocalname) {
        String localname = null;
        Unit.Definition def = null;
        Unit existingUnit = null;
        if (nonstandardLocalname != null) {
            localname = nonstandardLocalname;
            existingUnit = Qudt.unitFromLocalname(localname).orElse(null);
        } else {
            factorUnits.getLocalname();
            existingUnit = findUnitByLocalName(factorUnits);
        }
        if (existingUnit == null) {
            def = Unit.definition(QudtNamespaces.unit.makeIriInNamespace(localname));
            FactorUnits base = getBaseFactorUnits(factorUnits).withoutScaleFactor();
            if (base.equals(factorUnits)) {
                def.conversionMultiplier(findMultiplier(factorUnits));
            } else {
                List<Unit> units =
                        Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.ALL, base.getFactorUnits());
                units = filterPossibleBasesByQkIfOnlyOneFactor(factorUnits, units);
                if (units.isEmpty()) {
                    throw new RuntimeException(
                            String.format(
                                    "Cannot create unit %s: the factor units %s define a scaled unit, but there is no QUDT unit for the base factor units %s. Add a unit for these factors first.",
                                    localname,
                                    factorUnits.toString(),
                                    base.getFactorUnits().toString()));
                }
                Unit scalingOf = units.stream().findFirst().get();
                def.conversionMultiplier(
                        factorUnits
                                .conversionFactor(scalingOf)
                                .multiply(scalingOf.getConversionMultiplier().get()));
                def.scalingOf(scalingOf);
            }
            def.setFactorUnits(factorUnits).dimensionVectorIri(factorUnits.getDimensionVectorIri());
        } else {
            def = Unit.definition(existingUnit.getIri());
            final Unit.Definition finalDef = def;
            existingUnit
                    .getFactorUnits()
                    .getFactorUnits()
                    .forEach(fu -> finalDef.addFactorUnit(fu));
            FactorUnits base = getBaseFactorUnits(factorUnits).withoutScaleFactor();
            if (existingUnit.getScalingOf().isPresent()) {
                finalDef.scalingOf(existingUnit.getScalingOf().get());
                finalDef.conversionMultiplier(
                        factorUnits
                                .conversionFactor(existingUnit.getScalingOf().get())
                                .multiply(
                                        existingUnit
                                                .getScalingOf()
                                                .get()
                                                .getConversionMultiplier()
                                                .get()));
                existingUnit.getConversionOffset().ifPresent(co -> finalDef.conversionOffset(co));
            } else {
                List<Unit> baseUnits =
                        Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.ALL, base.getFactorUnits());
                baseUnits = filterPossibleBasesByQkIfOnlyOneFactor(factorUnits, baseUnits);
                Optional<Unit> newBase = baseUnits.stream().findFirst();
                if (newBase.isEmpty() || newBase.get().equals(existingUnit)) {
                    finalDef.conversionMultiplier(findMultiplier(factorUnits));
                } else {
                    finalDef.scalingOf(newBase.get());
                    finalDef.conversionMultiplier(
                            factorUnits
                                    .conversionFactor(newBase.get())
                                    .multiply(newBase.get().getConversionMultiplier().get()));
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

    private static List<Unit> filterPossibleBasesByQkIfOnlyOneFactor(
            FactorUnits factorUnits, List<Unit> baseUnits) {
        if (factorUnits.getFactorUnits().size() == 1) {
            // make sure the base we found shares the quantity kinds of the base we provided
            baseUnits =
                    baseUnits.stream()
                            .filter(
                                    bu ->
                                            factorUnits
                                                    .getFactorUnits()
                                                    .get(0)
                                                    .getUnit()
                                                    .getQuantityKinds()
                                                    .stream()
                                                    .allMatch(
                                                            qk ->
                                                                    bu.getQuantityKinds()
                                                                            .contains(qk)))
                            .collect(Collectors.toList());
        }
        return baseUnits;
    }

    private static BigDecimal findMultiplier(FactorUnits factorUnits) {
        String dimensionVectorIri = factorUnits.getDimensionVectorIri();
        Set<Unit> possibleBases =
                Qudt.allUnits().stream()
                        .filter(
                                u ->
                                        u.getDimensionVectorIri()
                                                .map(dv -> dv.equals(dimensionVectorIri))
                                                .orElse(false))
                        .filter(
                                u ->
                                        factorUnits
                                                .streamLocalnamePossibilities()
                                                .noneMatch(u.getIriLocalname()::equals))
                        .filter(
                                u ->
                                        u.getConversionMultiplier().get().compareTo(BigDecimal.ONE)
                                                == 0)
                        .collect(Collectors.toSet());
        if (possibleBases.isEmpty()) {
            return BigDecimal.ONE;
        }
        for (Unit base : possibleBases) {
            try {
                return factorUnits.conversionFactor(base);
            } catch (Exception e) {
            }
        }
        throw new RuntimeException(
                String.format(
                        "Unable to calculate conversion factor from these factor units %s to any of these bases %s",
                        factorUnits.toString(),
                        possibleBases.stream()
                                .map(Unit::getIriAbbreviated)
                                .collect(Collectors.joining(", "))));
    }

    private static Unit findUnitByLocalName(FactorUnits factorUnits) {
        return factorUnits
                .streamLocalnamePossibilities()
                .map(
                        localNameCandidate ->
                                Qudt.unitFromLocalname(localNameCandidate)
                                        .orElseGet(
                                                () ->
                                                        Qudt.currencyFromLocalname(
                                                                        localNameCandidate)
                                                                .orElse(null)))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
