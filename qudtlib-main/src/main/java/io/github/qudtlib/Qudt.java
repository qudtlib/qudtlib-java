package io.github.qudtlib;

import static java.util.stream.Collectors.toList;

import io.github.qudtlib.algorithm.AssignmentProblem;
import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.init.Initializer;
import io.github.qudtlib.model.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main QUDTLib interface.
 *
 * <p>A few examples:
 *
 * <pre>{@code
 * // Converting 38.5° Celsius into Fahrenheit:
 * Qudt.convert(new BigDecimal("38.5"), Qudt.Units.DEG_C, Qudt.Units.DEG_F);
 * // finding unit for factors: m, kg, and s^-2:
 * Set<Unit> myUnits =
 *            Qudt.derivedUnit(
 *                    Qudt.Units.M, 1,
 *                    Qudt.Units.KiloGM, 1,
 *                    Qudt.Units.SEC, -2);
 * // finding factors of Newton:
 * List<FactorUnit> myFactorUnits = Qudt.Units.N.getFactorUnits();
 * // Converting 1N into kN (using QuantityValue):
 * QuantityValue quantityValue = new QuantityValue(new BigDecimal("1"), Qudt.Units.N);
 * QuantityValue converted = Qudt.convert(quantityValue, Qudt.Units.KiloN);
 * }</pre>
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Qudt {
    private static final Map<String, Unit> units;
    private static final Map<String, QuantityKind> quantityKinds;
    private static final Map<String, Prefix> prefixes;
    private static final Map<String, SystemOfUnits> systemsOfUnits;
    private static final BigDecimal BD_1000 = new BigDecimal("1000");

    public abstract static class NAMESPACES extends QudtNamespaces {}

    /*
     * The constants for units, quantity kinds and prefixes are kept in separate package-protected classes as they are
     * quite numerous and would clutter this class too much. They are made available
     */
    public abstract static class Units extends io.github.qudtlib.model.Units {}

    public abstract static class QuantityKinds extends io.github.qudtlib.model.QuantityKinds {}

    public abstract static class Prefixes extends io.github.qudtlib.model.Prefixes {}

    public abstract static class SystemsOfUnits extends io.github.qudtlib.model.SystemsOfUnits {}

    /* Use the Initializer to load and wire all prefixes, units and quantityKinds. */
    static {
        Initializer initializer = null;
        try {
            Class<?> type = Class.forName("io.github.qudtlib.init.InitializerImpl");
            initializer = (Initializer) type.getConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot initialize QUDTlib\n\nMake sure you have either \n\n\tqudtlib-init-rdf \nor \n\n\tqudtlib-init-hardcoded \n\non your classpath!",
                    e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Cannot initialize QUDTlib - No zero-arg constructor found in InitializerImpl",
                    e);
        }
        if (initializer != null) {
            Initializer.Definitions definitions = initializer.loadData();
            prefixes = initializer.buildPrefixes(definitions);
            units = initializer.buildUnits(definitions);
            quantityKinds = initializer.buildQuantityKinds(definitions);
            systemsOfUnits = initializer.buildSystemsOfUnits(definitions);
        } else {
            prefixes = new HashMap<>();
            units = new HashMap<>();
            quantityKinds = new HashMap<>();
            systemsOfUnits = new HashMap<>();
            System.err.println(
                    "\n\n\n ERROR: The QUDTlib data model has not been initialized properly and will not work\n\n\n");
        }
    }

    /*
     * public methods
     */

    /**
     * Returns a {@link Unit} for the specified localname (i.e. the last element of the Unit IRI).
     * For example, <code>unitFromLocalName("N-PER-M2")</code> yields the unit with IRI <code>
     * http://qudt.org/vocab/unit/N-PER-M2</code>.
     *
     * @param localname the local name of the IRI that identifies the requested unit.
     * @return the unit
     * @throws NotFoundException if no such unit is found.
     */
    public static Optional<Unit> unitFromLocalname(String localname) {
        return unit(unitIriFromLocalname(localname));
    }

    public static Unit unitFromLocalnameRequired(String localname) {
        return unitRequired(unitIriFromLocalname(localname));
    }

    /**
     * Returns the first unit found whose label matches the specified label after replacing any
     * underscore with space and ignoring case (US locale). If more intricate matching is needed,
     * clients can use <code>{@link #allUnits()}.stream().filter(...)</code>.
     *
     * @param label the matched label
     * @return the first unit found
     */
    public static Optional<Unit> unitFromLabel(String label) {
        LabelMatcher labelMatcher = new LabelMatcher(label);
        return units.values().stream()
                .filter(u -> u.getLabels().stream().anyMatch(labelMatcher::matches))
                .findFirst();
    }

    public static Unit unitFromLabelRequired(String label) {
        return unitFromLabel(label)
                .orElseThrow(
                        () -> new NotFoundException("No unit found for label '" + label + "'"));
    }

    /**
     * Returns the {@link Unit} identified the specified IRI. For example, <code>
     * unit("http://qudt.org/vocab/unit/N-PER-M2")</code> yields {@code Qudt.Units.N__PER__M2};
     *
     * @param iri the requested unit IRI
     * @return the unit
     */
    public static Optional<Unit> unit(String iri) {
        return Optional.ofNullable(units.get(iri));
    }

    public static Unit unitRequired(String iri) {
        return Optional.ofNullable(units.get(iri))
                .orElseThrow(() -> new NotFoundException("No unit found for Iri " + iri));
    }

    /**
     * Returns a unit IRI with the specified localname (even if no such unit exists in the model).
     *
     * @param localname the local name of the IRI that identifies the requested unit.
     * @return the full IRI, possibly identifying a unit
     */
    public static String unitIriFromLocalname(String localname) {
        return NAMESPACES.unit.makeIriInNamespace(localname);
    }

    public static Unit scale(String prefixLabel, String baseUnitLabel) {
        LabelMatcher labelMatcher = new LabelMatcher(baseUnitLabel);
        return units.values().stream()
                .filter(u -> u.getPrefix().isPresent())
                .filter(
                        u ->
                                u.getPrefix().get().getLabels().stream()
                                        .anyMatch(
                                                pl -> pl.getString().equalsIgnoreCase(prefixLabel)))
                .filter(u -> u.getScalingOf().isPresent())
                .filter(
                        u ->
                                u.getScalingOf().get().getLabels().stream()
                                        .anyMatch(labelMatcher::matches))
                .findFirst()
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        String.format(
                                                "No scaled unit found with base unit '%s' and prefix '%s'",
                                                baseUnitLabel, prefixLabel)));
    }

    /**
     * Returns the unit resulting from scaling the specified {@code unit} with the specified {@code
     * prefix}.
     *
     * @param prefix the prefix to use for scaling
     * @param baseUnit the unit to scale
     * @return the resulting unit
     * @throws NotFoundException if no such unit is present in the model.
     */
    public static Unit scale(Prefix prefix, Unit baseUnit) {
        return units.values().stream()
                .filter(u -> u.getPrefix().isPresent())
                .filter(u -> u.getPrefix().get().equals(prefix))
                .filter(u -> u.getScalingOf().isPresent())
                .filter(u -> u.getScalingOf().get().equals(baseUnit))
                .findFirst()
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        String.format(
                                                "No scaled unit found with base unit '%s' and prefix '%s'",
                                                baseUnit, prefix)));
    }

    /**
     * Returns the base unit of the specified scaled {@code unit}. For example, {@code
     * unscale(Qudt.Units.KiloM)} returns {@code Qudt.Units.M}
     *
     * @param unit the scaled unit
     * @return the base unit
     */
    public static Unit unscale(Unit unit) {
        if (unit.getScalingOf().isEmpty()) {
            return unit;
        }
        return unit.getScalingOf().get();
    }

    /**
     * Returns the list of {@link FactorUnit}s of the specified {@code unit}.
     *
     * @param unit the unit to get factors for
     * @return the factors of the unit or an empty list if the unit is not a derived unit
     */
    public static List<FactorUnit> factorUnits(Unit unit) {
        return simplifyFactorUnits(unit.getLeafFactorUnitsWithCumulativeExponents());
    }

    /**
     * Perform mathematical simplification on factor units. For example, {@code N per M per M -> N
     * per M^2 }
     *
     * @param factorUnits the factor units to simplify
     * @return the simplified factor units.
     */
    public static List<FactorUnit> simplifyFactorUnits(List<FactorUnit> factorUnits) {
        return new ArrayList<>(
                factorUnits.stream()
                        .collect(
                                Collectors.toMap(
                                        FactorUnit::getKind,
                                        Function.identity(),
                                        FactorUnit::combine))
                        .values());
    }

    /**
     * Return a list of {@link FactorUnit}s with the same exponents as the specified {@code
     * factorUnits} but their base units as units.
     *
     * @param factorUnits the factor units to unscale
     * @return the unscaled factor units
     */
    public static List<FactorUnit> unscale(List<FactorUnit> factorUnits) {
        return factorUnits.stream()
                .map(
                        uf ->
                                FactorUnit.builder()
                                        .unit(unscale(uf.getUnit()))
                                        .exponent(uf.getExponent())
                                        .build())
                .collect(toList());
    }

    /**
     * Obtains units based on factor units.
     *
     * <p>For example,
     *
     * <pre>{@code
     * Qudt.derivedUnitsFrom Map(
     *                     FactorUnitMatchingMode.EXACT,
     *                     Map.of(
     *                     Qudt.Units.M, 1,
     *                     Qudt.Units.KiloGM, 1,
     *                     Qudt.Units.SEC, -2));
     * }</pre>
     *
     * will yield a Set containing the Newton Unit ({@code Qudt.Units.N})
     *
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param factorUnits a map containing unit to exponent entries.
     * @return the derived units that match the given factor units
     */
    public static Set<Unit> derivedUnitsFromMap(
            DerivedUnitSearchMode searchMode, Map<Unit, Integer> factorUnits) {
        Object[] arr = new Object[factorUnits.size() * 2];
        return derivedUnitsFromUnitExponentPairs(
                searchMode,
                factorUnits.entrySet().stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    /**
     * Obtains units based on factor units.
     *
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param factorUnits the factor units
     * @return the derived unit that match the given factor units
     * @see #derivedUnitsFromMap(DerivedUnitSearchMode, Map)
     */
    public static Set<Unit> derivedUnitsFromFactorUnits(
            DerivedUnitSearchMode searchMode, List<FactorUnit> factorUnits) {
        FactorUnits selection = new FactorUnits(factorUnits);
        return derivedUnitsFromFactorUnits(searchMode, selection);
    }

    /**
     * Vararg method, must be an even number of arguments, always alternating types of Unit|String
     * and Integer.
     *
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param factorUnitSpec alternating (unit, exponent) pairs. The unit can be specified as {@link
     *     Unit} or String. In the latter case, it can be a unit IRI, a unit IRI's local name or a
     *     unit's label. The exponent must be an and Integer.
     * @return the units that match
     * @see #derivedUnitsFromMap(DerivedUnitSearchMode, Map)
     */
    public static Set<Unit> derivedUnitsFromUnitExponentPairs(
            DerivedUnitSearchMode searchMode, final Object... factorUnitSpec) {
        Object[] spec = new Object[factorUnitSpec.length];
        for (int i = 0; i < factorUnitSpec.length; i++) {
            if (i % 2 == 0 && factorUnitSpec[i] instanceof Unit) {
                spec[i] = factorUnitSpec[i];
            } else if (i % 2 == 0 && factorUnitSpec[i] instanceof String) {
                String unitString = (String) factorUnitSpec[i];
                Optional<Unit> unitOpt = unit(unitString);
                if (unitOpt.isEmpty()) {
                    unitOpt = unitFromLocalname(unitString);
                }
                if (unitOpt.isEmpty()) {
                    unitOpt = unitFromLabel(unitString);
                }
                if (unitOpt.isEmpty()) {
                    throw new NotFoundException(
                            String.format(
                                    "Unable to find unit for string %s, interpreted as iri, label, or localname",
                                    unitString));
                }
                spec[i] = unitOpt.get();
            } else if (i % 2 == 1 && factorUnitSpec[i] instanceof Integer) {
                spec[i] = factorUnitSpec[i];
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "Cannot handle input '%s' at 0-base position %d",
                                factorUnitSpec[i].toString(), i));
            }
        }
        FactorUnits selection = FactorUnits.ofFactorUnitSpec(spec);
        return derivedUnitsFromFactorUnits(searchMode, selection);
    }

    /**
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param selection the factor unit selection
     * @return the units that match
     * @see #derivedUnitsFromMap(DerivedUnitSearchMode, Map)
     */
    private static Set<Unit> derivedUnitsFromFactorUnits(
            DerivedUnitSearchMode searchMode, FactorUnits selection) {
        List<Unit> matchingUnits =
                units.values().stream()
                        .filter(d -> d.matches(selection))
                        .collect(Collectors.toList());
        if (searchMode == DerivedUnitSearchMode.ALL || matchingUnits.size() < 2) {
            return new HashSet<>(matchingUnits);
        }
        Map<Unit, Double> scores = new HashMap<>();
        for (Unit unit : matchingUnits) {
            scores.put(unit, matchScore(unit, selection));
        }
        return Set.of(
                matchingUnits.stream()
                        .max((l, r) -> (int) Math.signum(scores.get(l) - scores.get(r)))
                        .get());
    }

    private static Double matchScore(Unit unit, FactorUnits requested) {
        List<List<FactorUnit>> unitFactors = unit.getAllPossibleFactorUnitCombinations();
        List<List<FactorUnit>> requestedFactors =
                FactorUnit.getAllPossibleFactorUnitCombinations(requested.getFactorUnits());
        List<List<FactorUnit>> smaller =
                unitFactors.size() > requestedFactors.size() ? requestedFactors : unitFactors;
        List<List<FactorUnit>> larger =
                unitFactors.size() > requestedFactors.size() ? unitFactors : requestedFactors;
        double[][] unitSimilarityMatrix = getUnitSimilarityMatrix(smaller, larger);
        double overlapScore = 0;
        if (unitSimilarityMatrix.length > 0) {
            overlapScore = getOverlapScore(unitSimilarityMatrix);
        }
        String unitLocalName = getIriLocalName(unit.getIri());
        int tieBreaker =
                requested.getFactorUnits().stream()
                        .reduce(
                                0,
                                (prev, cur) ->
                                        prev
                                                + (unitLocalName.matches(
                                                                        ".*\\b"
                                                                                + getIriLocalName(
                                                                                        cur.getUnit()
                                                                                                .getIri())
                                                                                + "\\b.*")
                                                                || unitLocalName.matches(
                                                                        ".*\\b"
                                                                                + (getIriLocalName(
                                                                                                cur.getUnit()
                                                                                                        .getIri())
                                                                                        + Math.abs(
                                                                                                cur
                                                                                                        .getExponent()))
                                                                                + "\\b.*")
                                                        ? 1
                                                        : 0),
                                (l, r) -> l + r);
        return overlapScore
                + tieBreaker / Math.pow(unitFactors.size() + requestedFactors.size() + 1, 2);
    }

    private static double getOverlapScore(double[][] mat) {
        int numAssignments = mat.length;
        int rowsPlusCols = mat.length + mat[0].length;
        double minAssignmentScore = AssignmentProblem.instance(mat).solve().getWeight();
        double overlap =
                (double) numAssignments * (1 - (minAssignmentScore / (double) numAssignments));
        return overlap / ((double) rowsPlusCols - overlap);
    }

    static double[][] getUnitSimilarityMatrix(
            List<List<FactorUnit>> rows, List<List<FactorUnit>> cols) {
        return rows.stream()
                .map(
                        rowCombination ->
                                cols.stream()
                                        .map(
                                                colCombination ->
                                                        scoreCombinations(
                                                                rowCombination, colCombination))
                                        .mapToDouble(d -> d)
                                        .toArray())
                .collect(toList())
                .toArray(new double[0][0]);
    }

    private static double scoreCombinations(
            List<FactorUnit> leftFactors, List<FactorUnit> rightFactors) {
        List<FactorUnit> smaller =
                leftFactors.size() > rightFactors.size() ? rightFactors : leftFactors;
        List<FactorUnit> larger =
                leftFactors.size() > rightFactors.size() ? leftFactors : rightFactors;

        double[][] similarityMatrix =
                smaller.stream()
                        .map(
                                sFactor ->
                                        larger.stream()
                                                .map(
                                                        lFactor -> {
                                                            if (sFactor.equals(lFactor)) {
                                                                return 0.0;
                                                            }
                                                            Unit reqScaledOrSelf =
                                                                    sFactor.getUnit()
                                                                            .getScalingOf()
                                                                            .orElse(
                                                                                    sFactor
                                                                                            .getUnit());
                                                            Unit unitScaledOrSelf =
                                                                    lFactor.getUnit()
                                                                            .getScalingOf()
                                                                            .orElse(
                                                                                    lFactor
                                                                                            .getUnit());
                                                            if (reqScaledOrSelf.equals(
                                                                            unitScaledOrSelf)
                                                                    && sFactor.getExponent()
                                                                            == lFactor
                                                                                    .getExponent()) {
                                                                return 0.6;
                                                            }
                                                            if (sFactor.getUnit()
                                                                    .equals(lFactor.getUnit())) {
                                                                return 0.8;
                                                            }
                                                            if (reqScaledOrSelf.equals(
                                                                    unitScaledOrSelf)) {
                                                                return 0.9;
                                                            }
                                                            return 1.0;
                                                        })
                                                .mapToDouble(d -> d)
                                                .toArray())
                        .collect(toList())
                        .toArray(new double[0][0]);
        if (similarityMatrix.length == 0) {
            return 1;
        } else {
            // matrix values are between 0 and 1.
            // assignment is between 0 and (min(rows,cols))
            return 1 - getOverlapScore(similarityMatrix);
        }
    }

    private static String getIriLocalName(String iri) {
        return iri.replaceAll("^.+[/|#]", "");
    }

    /**
     * Returns the base unit of the specified {@code unit} along with the scale factor needed to
     * convert values from the base unit to the specified unit.
     *
     * @param unit the unit to scale to its base
     * @return a Map.Entry with the base unit and the required scale factor
     */
    public static Map.Entry<Unit, BigDecimal> scaleToBaseUnit(Unit unit) {
        if (!unit.isScaled()) {
            return Map.entry(unit, BigDecimal.ONE);
        }
        Unit baseUnit =
                unit.getScalingOf()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Scaled unit has null isScalingOf() unit - that's a bug!"));
        BigDecimal multiplier = unit.getConversionMultiplier(baseUnit);
        return Map.entry(baseUnit, multiplier);
    }

    /**
     * Returns a {@link QuantityKind} for the specified localname (i.e. the last element of the Unit
     * IRI). For example, <code>quantityKindFromLocalName("Width")</code> yields the quantityKind
     * with IRI <code>http://qudt.org/vocab/quantitykind/Width</code>.
     *
     * @param localname the local name of the IRI that identifies the requested quantityKind.
     * @return the quantityKind
     * @throws NotFoundException if no such quantityKind is found.
     */
    public static Optional<QuantityKind> quantityKindFromLocalname(String localname) {
        return quantityKind(quantityKindIriFromLocalname(localname));
    }

    public static QuantityKind quantityKindFromLocalnameRequired(String localname) {
        return quantityKindRequired(quantityKindIriFromLocalname(localname));
    }

    /**
     * Returns the {@link QuantityKind} identified the specified IRI. For example, <code>
     * quantityKind("http://qudt.org/vocab/quantitykind/Width")</code> yields {@code
     * Qudt.QuantityKinds.Width};
     *
     * @param iri the requested quantityKind IRI
     * @return the quantityKind
     * @throws NotFoundException if no such quantityKind is found.
     */
    public static Optional<QuantityKind> quantityKind(String iri) {
        return Optional.ofNullable(quantityKinds.get(iri));
    }

    public static QuantityKind quantityKindRequired(String iri) {
        return quantityKind(iri)
                .orElseThrow(() -> new NotFoundException("QuantityKind not found: " + iri));
    }

    /**
     * Returns the {@link QuantityKind}s associated with the specified {@link Unit}.
     *
     * @param unit the unit
     * @return the quantity kinds
     */
    public static Set<QuantityKind> quantityKinds(Unit unit) {
        return unit.getQuantityKinds().stream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the {@link QuantityKind}s associated with the specified {@link Unit}, transitively
     * following their <code>skos:broader</code> relationship.
     *
     * @param unit the unit
     * @return the quantity kinds
     */
    public static Set<QuantityKind> quantityKindsBroad(Unit unit) {
        Set<QuantityKind> current = quantityKinds(unit);
        Set<QuantityKind> result = new HashSet<>(current);
        while (!current.isEmpty()) {
            current =
                    current.stream()
                            .flatMap(qk -> qk.getBroaderQuantityKinds().stream())
                            .collect(Collectors.toSet());
            result.addAll(current);
        }
        return result;
    }

    /**
     * Returns a quantityKind IRI with the specified localname (even if no such quantityKind exists
     * in the model).
     *
     * @param localname the local name of the IRI that identifies the requested quantityKind.
     * @return the full IRI, possibly identifying a quantityKind
     */
    public static String quantityKindIriFromLocalname(String localname) {
        return NAMESPACES.quantityKind.makeIriInNamespace(localname);
    }

    /**
     * Returns a prefix IRI with the specified localname (even if no such prefix exists in the
     * model).
     *
     * @param localname the local name of the IRI that identifies the requested prefix.
     * @return the full IRI, possibly identifying a prefix
     */
    public static String prefixIriFromLocalname(String localname) {
        return NAMESPACES.prefix.makeIriInNamespace(localname);
    }

    /**
     * Returns a {@link Prefix} for the specified localname (i.e. the last element of the Unit IRI).
     * For example, <code>prefixFromLocalName("Mega")</code> yields the prefix with IRI <code>
     * http://qudt.org/vocab/prefix/Mega</code>.
     *
     * @param localname the local name of the IRI that identifies the requested prefix.
     * @return the prefix
     * @throws NotFoundException if no such prefix is found.
     */
    public static Prefix prefixFromLocalnameRequired(String localname) {
        return prefixRequired(prefixIriFromLocalname(localname));
    }

    public static Optional<Prefix> prefixFromLocalname(String localname) {
        return prefix(prefixIriFromLocalname(localname));
    }

    /**
     * Returns the {@link Prefix} identified the specified IRI. For example, <code>
     * prefix("http://qudt.org/vocab/prefix/Mega")</code> yields {@code Qudt.Prefixes.Mega};
     *
     * @param iri the requested prefix IRI
     * @return the prefix
     * @throws NotFoundException if no such prefix is found.
     */
    public static Optional<Prefix> prefix(String iri) {
        return Optional.ofNullable(prefixes.get(iri));
    }

    public static Prefix prefixRequired(String iri) {
        return prefix(iri).orElseThrow(() -> new NotFoundException("Prefix not found: " + iri));
    }

    /**
     * Returns a {@link SystemOfUnits} for the specified localname (i.e. the last element of the
     * SystemOfUnits IRI). For example, <code>systemOfUnitsFromLocalName("N-PER-M2")</code> yields
     * the systemOfUnits with IRI <code>
     * http://qudt.org/vocab/systemOfUnits/N-PER-M2</code>.
     *
     * @param localname the local name of the IRI that identifies the requested systemOfUnits.
     * @return the systemOfUnits
     * @throws NotFoundException if no such systemOfUnits is found.
     */
    public static Optional<SystemOfUnits> systemOfUnitsFromLocalname(String localname) {
        return systemOfUnits(systemOfUnitsIriFromLocalname(localname));
    }

    public static SystemOfUnits systemOfUnitsFromLocalnameRequired(String localname) {
        return systemOfUnitsRequired(systemOfUnitsIriFromLocalname(localname));
    }

    /**
     * Returns the first systemOfUnits found whose label matches the specified label after replacing
     * any underscore with space and ignoring case (US locale). If more intricate matching is
     * needed, clients can use <code>{@link #allSystemsOfUnits()}.stream().filter(...)</code>.
     *
     * @param label the matched label
     * @return the first systemOfUnits found
     */
    public static Optional<SystemOfUnits> systemOfUnitsFromLabel(String label) {
        LabelMatcher labelMatcher = new LabelMatcher(label);
        return systemsOfUnits.values().stream()
                .filter(u -> u.getLabels().stream().anyMatch(labelMatcher::matches))
                .findFirst();
    }

    public static SystemOfUnits systemOfUnitsFromLabelRequired(String label) {
        return systemOfUnitsFromLabel(label)
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "No systemOfUnits found for label '" + label + "'"));
    }

    /**
     * Returns the {@link SystemOfUnits} identified the specified IRI. For example, <code>
     * systemOfUnits("http://qudt.org/vocab/systemOfUnits/N-PER-M2")</code> yields {@code
     * Qudt.SystemOfUnitss.N__PER__M2};
     *
     * @param iri the requested systemOfUnits IRI
     * @return the systemOfUnits
     */
    public static Optional<SystemOfUnits> systemOfUnits(String iri) {
        return Optional.ofNullable(systemsOfUnits.get(iri));
    }

    public static SystemOfUnits systemOfUnitsRequired(String iri) {
        return Optional.ofNullable(systemsOfUnits.get(iri))
                .orElseThrow(() -> new NotFoundException("No systemOfUnits found for Iri " + iri));
    }

    /**
     * Returns a systemOfUnits IRI with the specified localname (even if no such systemOfUnits
     * exists in the model).
     *
     * @param localname the local name of the IRI that identifies the requested systemOfUnits.
     * @return the full IRI, possibly identifying a systemOfUnits
     */
    public static String systemOfUnitsIriFromLocalname(String localname) {
        return NAMESPACES.systemOfUnits.makeIriInNamespace(localname);
    }

    /**
     * Instantiates a {@link QuantityValue}.
     *
     * @param value the value
     * @param unitIri the Unit IRI
     * @return the resulting QuantityValue
     * @throws NotFoundException if no unit is found for the specified unitIri
     */
    public static QuantityValue quantityValue(BigDecimal value, String unitIri) {
        return new QuantityValue(value, unitRequired(unitIri));
    }

    /**
     * Instantiates a {@link QuantityValue}.
     *
     * @param value the value
     * @param unit the unit
     * @return the new quantity value
     */
    public static QuantityValue quantityValue(BigDecimal value, Unit unit) {
        return new QuantityValue(value, unit);
    }

    /**
     * Convert the specified {@link QuantityValue} <code>from</code> into the specified target
     * {@link Unit} <code>toUnit</code>.
     *
     * @param from the quantity value to convert
     * @param toUnit the target unit
     * @return a new {@link QuantityValue} object holding the result.
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     */
    public static QuantityValue convert(QuantityValue from, Unit toUnit)
            throws InconvertibleQuantitiesException {
        return from.convert(toUnit);
    }

    /**
     * Convert the specified {@link QuantityValue} <code>from</code> into the {@link Unit}
     * identified by the specified IRI <code>toUnitIri</code>.
     *
     * @param from the quantity value to convert
     * @param toUnitIri the IRI of the target unit
     * @return a new {@link QuantityValue} object holding the result.
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     * @throws NotFoundException if <code>toUnitIri</code> does not identify a unit in the model
     */
    public static QuantityValue convert(QuantityValue from, String toUnitIri)
            throws InconvertibleQuantitiesException, NotFoundException {
        Unit toUnit = unitRequired(toUnitIri);
        return quantityValue(convert(from.getValue(), from.getUnit(), toUnit), toUnit);
    }

    /**
     * Convert the specified <code>fromValue</code>, interpreted to be in the unit identified by
     * <code>fromUnitIri</code> into the unit identified by the specified IRI <code>toUnitIri</code>
     * .
     *
     * @param fromValue the value to convert
     * @param fromUnitIri the IRI of unit the <code>fromValue</code> is in
     * @param toUnitIri the IRI of the target unit
     * @return the resulting value
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     * @throws NotFoundException if <code>fromUnitIri</code> or <code>toUnitIri</code> does not
     *     identify a unit in the model
     */
    public static BigDecimal convert(BigDecimal fromValue, String fromUnitIri, String toUnitIri)
            throws InconvertibleQuantitiesException, NotFoundException {
        return convert(fromValue, unitRequired(fromUnitIri), unitRequired(toUnitIri));
    }

    /**
     * Convert the specified <code>fromValue</code>, interpreted to be in the {@link Unit} <code>
     * fromUnit</code> into the unit <code>toUnit</code>.
     *
     * @param fromValue the value to convert
     * @param fromUnit the unit of the <code>value</code>
     * @param toUnit the target unit
     * @return the resulting value
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     */
    public static BigDecimal convert(BigDecimal fromValue, Unit fromUnit, Unit toUnit)
            throws InconvertibleQuantitiesException {
        return fromUnit.convert(fromValue, toUnit);
    }

    /**
     * Indicates whether the two specified {@link Unit}s are convertible into each other.
     *
     * @param fromUnit a unit
     * @param toUnit another unit
     * @return <code>true</code> if the units are convertible.
     */
    public static boolean isConvertible(Unit fromUnit, Unit toUnit) {
        return fromUnit.isConvertible(toUnit);
    }

    /**
     * Returns a friendly message about QUDTLib.
     *
     * @return a greeting and some stats
     */
    public static String getGreeting() {
        return "This is QUDTLib-Java (https://github.com/qudtlib/qudtlib.java)\n"
                + "based on the QUDT ontology (https://qudt.org/)\n"
                + "happily providing\n"
                + "\t"
                + units.size()
                + " units\n"
                + "\t"
                + quantityKinds.size()
                + " quantityKinds\n"
                + "\t"
                + prefixes.size()
                + " prefixes\n";
    }

    private static class LabelMatcher {
        private final String labelToMatch;

        public LabelMatcher(String labelToMatch) {
            this.labelToMatch = labelToMatch.replaceAll("_", " ").toUpperCase(Locale.US);
        }

        public boolean matches(LangString candidateLabel) {
            return matches(candidateLabel.getString());
        }

        public boolean matches(String candiateLabel) {
            return candiateLabel.toUpperCase(Locale.US).equals(labelToMatch);
        }
    }

    static Map<String, Prefix> getPrefixesMap() {
        return Collections.unmodifiableMap(prefixes);
    }

    static Map<String, QuantityKind> getQuantityKindsMap() {
        return Collections.unmodifiableMap(quantityKinds);
    }

    static Map<String, Unit> getUnitsMap() {
        return Collections.unmodifiableMap(units);
    }

    static Map<String, SystemOfUnits> getSystemsOfUnitsMap() {
        return Collections.unmodifiableMap(systemsOfUnits);
    }

    /**
     * Returns all {@link Unit}s in the model.
     *
     * @return all units
     */
    public static Collection<Unit> allUnits() {
        return Collections.unmodifiableCollection(units.values());
    }

    /**
     * Returns all {@link QuantityKind}s in the model.
     *
     * @return all quantity kinds
     */
    public static Collection<QuantityKind> allQuantityKinds() {
        return Collections.unmodifiableCollection(quantityKinds.values());
    }

    /**
     * Returns all {@link Prefix}es in the model.
     *
     * @return all prefixes
     */
    public static Collection<Prefix> allPrefixes() {
        return Collections.unmodifiableCollection(prefixes.values());
    }

    /**
     * Returns all {@link SystemOfUnits}s in the model.
     *
     * @return all systemsOfUnits
     */
    public static Collection<SystemOfUnits> allSystemsOfUnits() {
        return Collections.unmodifiableCollection(systemsOfUnits.values());
    }

    public static Collection<Unit> allUnitsOfSystem(SystemOfUnits system) {
        return units.values().stream()
                .filter(system::allowsUnit)
                .collect(Collectors.toUnmodifiableSet());
    }
}
