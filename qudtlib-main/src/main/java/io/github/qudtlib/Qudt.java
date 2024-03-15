package io.github.qudtlib;

import static java.util.stream.Collectors.toList;

import com.java2s.Log10BigDecimal;
import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.init.Initializer;
import io.github.qudtlib.model.*;
import io.github.qudtlib.support.fractional.FractionalDimensionVector;
import io.github.qudtlib.support.fractional.FractionalUnits;
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

    private static final Map<String, ConstantValue> constantValues;

    private static final Map<String, PhysicalConstant> physicalConstants;

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

    public abstract static class PhysicalConstants
            extends io.github.qudtlib.model.PhysicalConstants {}

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
            constantValues = initializer.buildConstantValues(definitions);
            physicalConstants = initializer.buildPhysicalConstants(definitions);
        } else {
            prefixes = new HashMap<>();
            units = new HashMap<>();
            quantityKinds = new HashMap<>();
            systemsOfUnits = new HashMap<>();
            constantValues = new HashMap<>();
            physicalConstants = new HashMap<>();
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

    public static Unit currencyFromLocalnameRequired(String localname) {
        return unitRequired(currencyIriFromLocalname(localname));
    }

    public static Optional<Unit> currencyFromLocalname(String localname) {
        return unit(currencyIriFromLocalname(localname));
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

    public static String currencyIriFromLocalname(String localname) {
        return NAMESPACES.currency.makeIriInNamespace(localname);
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
     * unscale(Qudt.Units.KiloM)} returns {@code Qudt.Units.M}. {@code Qudt.Units.KiloGM} as well as
     * any unit that does not have a prefix (such as {@code Qudt.Units.HR}) are treated as
     * non-scaled units, i.e. returned directly.
     *
     * @param unit the scaled unit
     * @return the base unit
     */
    public static Unit unscale(Unit unit) {
        return unscale(unit, true, true);
    }

    /**
     * Returns the base unit of the specified scaled {@code unit}. For example, {@code
     * unscale(Qudt.Units.KiloM)} returns {@code Qudt.Units.M}. The parameter {@code
     * treatKiloGmAsUnscaled} and {@code treatPrefixlessAsUnscaled} decide whether {@code
     * Qudt.Units.KiloGM} and units without prefixes (such as {@code Qudt.Units.HR}), respectively,
     * are treated a non-scaled units. directly.
     *
     * @param unit
     * @param treatKiloGmAsUnscaled
     * @param treatPrefixlessAsUnscaled
     * @return
     */
    public static Unit unscale(
            Unit unit, boolean treatKiloGmAsUnscaled, boolean treatPrefixlessAsUnscaled) {
        if (unit.getScalingOf().isEmpty()) {
            return unit;
        }
        if (treatPrefixlessAsUnscaled && unit.getPrefix().isEmpty()) {
            return unit;
        }
        if (treatKiloGmAsUnscaled && unit.getIriAbbreviated().equals("unit:KiloGM")) {
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
        return unscale(factorUnits, true, true);
    }

    public static List<FactorUnit> unscale(
            List<FactorUnit> factorUnits,
            boolean treatKiloGmAsUnscaled,
            boolean treatPrefixlessAsUnscaled) {
        return factorUnits.stream()
                .map(
                        uf ->
                                FactorUnit.builder()
                                        .unit(
                                                unscale(
                                                        uf.getUnit(),
                                                        treatKiloGmAsUnscaled,
                                                        treatPrefixlessAsUnscaled))
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
    public static List<Unit> unitsFromMap(
            DerivedUnitSearchMode searchMode, Map<Unit, Integer> factorUnits) {
        Object[] arr = new Object[factorUnits.size() * 2];
        return unitsFromUnitExponentPairs(
                searchMode,
                factorUnits.entrySet().stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    /**
     * Deprecated - use <code>Qudt.unitsFromMap({@link DerivedUnitSearchMode}, FactorUnits)</code>
     * instead.
     *
     * @param searchMode
     * @param factorUnits
     * @return
     */
    @Deprecated(since = "6.2", forRemoval = true)
    public static Set<Unit> derivedUnitsFromMap(
            DerivedUnitSearchMode searchMode, Map<Unit, Integer> factorUnits) {
        return new HashSet<>(unitsFromMap(searchMode, factorUnits));
    }

    /**
     * Obtains units based on factor units.
     *
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param factorUnits the factor units
     * @return the derived unit that match the given factor units
     * @see #unitsFromMap(DerivedUnitSearchMode, Map)
     */
    public static List<Unit> unitsFromFactorUnits(
            DerivedUnitSearchMode searchMode, List<FactorUnit> factorUnits) {
        FactorUnits selection = new FactorUnits(factorUnits);
        return derivedUnitListFromFactorUnits(searchMode, selection);
    }

    /**
     * Deprecated - use <code>Qudt.unitsFromFactorUnits({@link DerivedUnitSearchMode}, FactorUnits)
     * </code> instead.
     *
     * @param searchMode
     * @param factorUnits
     * @return
     */
    @Deprecated(since = "6.2", forRemoval = true)
    public static Set<Unit> derivedUnitsFromFactorUnits(
            DerivedUnitSearchMode searchMode, List<FactorUnit> factorUnits) {
        return new HashSet<>(unitsFromFactorUnits(searchMode, factorUnits));
    }

    /**
     * Vararg method, must be an even number of arguments, always alternating types of Unit|String
     * and Integer.
     *
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param factorUnitSpec alternating (unit, exponent) pairs. The unit can be specified as {@link
     *     Unit} or String. In the latter case, it can be a unit IRI, a unit IRI's local name or a
     *     unit's label. The exponent must be an Integer.
     * @return the units that match
     * @see #unitsFromMap(DerivedUnitSearchMode, Map)
     */
    public static List<Unit> unitsFromUnitExponentPairs(
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
        return derivedUnitListFromFactorUnits(searchMode, selection);
    }

    /**
     * Deprecated - use <code>
     * Qudt.unitsFromUnitExponentPairs({@link DerivedUnitSearchMode}, Object...</code> instead.
     *
     * @param searchMode
     * @param factorUnitSpec
     * @return
     */
    @Deprecated(since = "6.2", forRemoval = true)
    public static Set<Unit> derivedUnitsFromUnitExponentPairs(
            DerivedUnitSearchMode searchMode, final Object... factorUnitSpec) {
        return new HashSet<>(unitsFromUnitExponentPairs(searchMode, factorUnitSpec));
    }

    /**
     * @param searchMode the {@link DerivedUnitSearchMode} to use
     * @param selection the factor unit selection
     * @return the units that match
     * @see #unitsFromMap(DerivedUnitSearchMode, Map)
     */
    private static List<Unit> derivedUnitListFromFactorUnits(
            DerivedUnitSearchMode searchMode, FactorUnits selection) {

        List<Unit> matchingUnits =
                units.values().stream()
                        .filter(d -> d.matches(selection))
                        .collect(Collectors.toList());
        if (searchMode == DerivedUnitSearchMode.ALL || matchingUnits.size() < 2) {
            return matchingUnits.stream()
                    .sorted(bestMatchForFactorUnitsComparator(selection))
                    .collect(toList());
        }

        return matchingUnits.stream().min(bestMatchForFactorUnitsComparator(selection)).stream()
                .collect(Collectors.toList());
    }

    public static List<Unit> unitsWithSameFractionalDimensionVector(Unit unit) {
        Objects.requireNonNull(unit);
        FractionalDimensionVector fdv = FractionalUnits.getFractionalDimensionVector(unit);
        return Qudt.units.values().stream()
                .filter(
                        u -> {
                            try {
                                return fdv.equals(FractionalUnits.getFractionalDimensionVector(u));
                            } catch (Exception e) {
                                return false;
                            }
                        })
                .collect(Collectors.toList());
    }

    private static Comparator<Unit> bestMatchForFactorUnitsComparator(
            FactorUnits requestedFactorUnits) {

        FactorUnits reqNorm = requestedFactorUnits.normalize();
        FactorUnits reqNum = requestedFactorUnits.numerator();
        FactorUnits reqNumNorm = reqNum.normalize();
        FactorUnits reqDen = requestedFactorUnits.denominator();
        FactorUnits reqDenNorm = reqDen.normalize();
        List<String> reqLocalNamePossibilities =
                requestedFactorUnits.generateAllLocalnamePossibilities();
        return new Comparator<Unit>() {
            @Override
            public int compare(Unit left, Unit right) {
                if (left.getFactorUnits().equals(requestedFactorUnits)) {
                    if (!right.getFactorUnits().equals(requestedFactorUnits)) {
                        return -1;
                    }
                } else {
                    if (right.getFactorUnits().equals(requestedFactorUnits)) {
                        return 1;
                    }
                }
                if (!left.getIriLocalname().contains("-")) {
                    if (right.getIriLocalname().contains("-")) {
                        return -1; // prefer a derived unit with a new name (such as W, J, N etc.)
                    }
                } else if (!right.getIriLocalname().contains("-")) {
                    return 1;
                }

                FactorUnits leftDen = left.getFactorUnits().denominator();
                FactorUnits rightDen = right.getFactorUnits().denominator();
                int leftFactorsDenCnt = leftDen.expand().size();
                int rightFactorsDenCnt = rightDen.expand().size();
                int reqFactorsDenCnt = reqDen.expand().size();
                int diffFactorsCountDen =
                        Math.abs(reqFactorsDenCnt - leftFactorsDenCnt)
                                - Math.abs(reqFactorsDenCnt - rightFactorsDenCnt);
                if (diffFactorsCountDen != 0) {
                    return diffFactorsCountDen;
                }

                FactorUnits leftNum = left.getFactorUnits().numerator();
                FactorUnits rightNum = right.getFactorUnits().denominator();
                int leftFactorsNumCnt = leftNum.expand().size();
                int rightFactorsNumCnt = rightNum.expand().size();
                int reqFactorsNumCnt = reqNum.expand().size();
                int diffFactorsCountNum =
                        Math.abs(reqFactorsNumCnt - leftFactorsNumCnt)
                                - Math.abs(reqFactorsNumCnt - rightFactorsNumCnt);
                if (diffFactorsCountNum != 0) {
                    return diffFactorsCountNum;
                }
                int leftCnt = left.getFactorUnits().expand().size();
                int rightCnt = right.getFactorUnits().expand().size();
                int reqCnt = requestedFactorUnits.expand().size();
                if (leftCnt == reqCnt) {
                    if (rightCnt != reqCnt) {
                        return -1;
                    }
                } else {
                    if (rightCnt == reqCnt) {
                        return 1;
                    }
                }
                if (reqLocalNamePossibilities.contains(left.getIriLocalname())) {
                    if (!reqLocalNamePossibilities.contains(right.getIriLocalname())) {
                        return -1;
                    }
                } else if (reqLocalNamePossibilities.contains(right.getIriLocalname())) {
                    return 1;
                }
                return left.getIriLocalname().compareTo(right.getIriLocalname());
            }
        };
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
     * Returns a constantValue IRI with the specified localname (even if no such constantValue
     * exists in the model).
     *
     * @param localname the local name of the IRI that identifies the requested constantValue.
     * @return the full IRI, possibly identifying a constantValue
     */
    public static String constantValueIriFromLocalname(String localname) {
        return NAMESPACES.constant.makeIriInNamespace(localname);
    }

    /**
     * Returns a {@link ConstantValue} for the specified localname (i.e. the last element of the
     * Unit IRI). For example, <code>constantValueFromLocalName("Mega")</code> yields the
     * constantValue with IRI <code>
     * http://qudt.org/vocab/constantValue/Mega</code>.
     *
     * @param localname the local name of the IRI that identifies the requested constantValue.
     * @return the constantValue
     * @throws NotFoundException if no such constantValue is found.
     */
    public static ConstantValue constantValueFromLocalnameRequired(String localname) {
        return constantValueRequired(constantValueIriFromLocalname(localname));
    }

    public static Optional<ConstantValue> constantValueFromLocalname(String localname) {
        return constantValue(constantValueIriFromLocalname(localname));
    }

    /**
     * Returns the {@link ConstantValue} identified the specified IRI. For example, <code>
     * constantValue("http://qudt.org/vocab/constantValue/Mega")</code> yields {@code
     * Qudt.ConstantValuees.Mega};
     *
     * @param iri the requested constantValue IRI
     * @return the constantValue
     * @throws NotFoundException if no such constantValue is found.
     */
    public static Optional<ConstantValue> constantValue(String iri) {
        return Optional.ofNullable(constantValues.get(iri));
    }

    public static ConstantValue constantValueRequired(String iri) {
        return constantValue(iri)
                .orElseThrow(() -> new NotFoundException("ConstantValue not found: " + iri));
    }

    /**
     * Returns a physicalConstant IRI with the specified localname (even if no such physicalConstant
     * exists in the model).
     *
     * @param localname the local name of the IRI that identifies the requested physicalConstant.
     * @return the full IRI, possibly identifying a physicalConstant
     */
    public static String physicalConstantIriFromLocalname(String localname) {
        return NAMESPACES.constant.makeIriInNamespace(localname);
    }

    /**
     * Returns a {@link PhysicalConstant} for the specified localname (i.e. the last element of the
     * Unit IRI). For example, <code>physicalConstantFromLocalName("Mega")</code> yields the
     * physicalConstant with IRI <code>
     * http://qudt.org/vocab/physicalConstant/Mega</code>.
     *
     * @param localname the local name of the IRI that identifies the requested physicalConstant.
     * @return the physicalConstant
     * @throws NotFoundException if no such physicalConstant is found.
     */
    public static PhysicalConstant physicalConstantFromLocalnameRequired(String localname) {
        return physicalConstantRequired(physicalConstantIriFromLocalname(localname));
    }

    public static Optional<PhysicalConstant> physicalConstantFromLocalname(String localname) {
        return physicalConstant(physicalConstantIriFromLocalname(localname));
    }

    /**
     * Returns the {@link PhysicalConstant} identified the specified IRI. For example, <code>
     * physicalConstant("http://qudt.org/vocab/physicalConstant/Mega")</code> yields {@code
     * Qudt.PhysicalConstantes.Mega};
     *
     * @param iri the requested physicalConstant IRI
     * @return the physicalConstant
     * @throws NotFoundException if no such physicalConstant is found.
     */
    public static Optional<PhysicalConstant> physicalConstant(String iri) {
        return Optional.ofNullable(physicalConstants.get(iri));
    }

    public static PhysicalConstant physicalConstantRequired(String iri) {
        return physicalConstant(iri)
                .orElseThrow(() -> new NotFoundException("PhysicalConstant not found: " + iri));
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
        return convert(from, toUnit, null);
    }

    /**
     * Convert the specified {@link QuantityValue} <code>from</code> into the specified target
     * {@link Unit} <code>toUnit</code>.
     *
     * @param from the quantity value to convert
     * @param toUnit the target unit
     * @param quantityKind optional quantity kind for handling edge cases (temperature difference)
     * @return a new {@link QuantityValue} object holding the result.
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     */
    public static QuantityValue convert(QuantityValue from, Unit toUnit, QuantityKind quantityKind)
            throws InconvertibleQuantitiesException {
        return from.convert(toUnit, quantityKind);
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
        return convert(fromValue, fromUnit, toUnit, null);
    }

    /**
     * Convert the specified <code>fromValue</code>, interpreted to be in the {@link Unit} <code>
     * fromUnit</code> into the unit <code>toUnit</code>.
     *
     * @param fromValue the value to convert
     * @param fromUnit the unit of the <code>value</code>
     * @param toUnit the target unit
     * @param quantityKind optional quantity kind for handling edge cases (temperature difference)
     * @return the resulting value
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     */
    public static BigDecimal convert(
            BigDecimal fromValue, Unit fromUnit, Unit toUnit, QuantityKind quantityKind)
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

    static Map<String, PhysicalConstant> getPhysicalConstantsMap() {
        return Collections.unmodifiableMap(physicalConstants);
    }

    static Map<String, ConstantValue> getConstantValuesMap() {
        return Collections.unmodifiableMap(constantValues);
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

    public static Collection<PhysicalConstant> allPhysicalConstant() {
        return Collections.unmodifiableCollection(physicalConstants.values());
    }

    public static Collection<ConstantValue> allConstantValues() {
        return Collections.unmodifiableCollection(constantValues.values());
    }

    public static Collection<Unit> allUnitsOfSystem(SystemOfUnits system) {
        return units.values().stream()
                .filter(system::allowsUnit)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the first unit obtained using {@link #correspondingUnitsInSystem(Unit,
     * SystemOfUnits)}.
     *
     * @return the unit corresponding to the specified unit in the specified systemOfUnits.
     */
    public static Optional<Unit> correspondingUnitInSystem(Unit unit, SystemOfUnits systemOfUnits) {
        return correspondingUnitsInSystem(unit, systemOfUnits).stream().findFirst();
    }

    /**
     * Gets units that correspond to the specified unit are allowed in the specified systemOfUnits.
     * The resulting units have to
     *
     * <ol>
     *   <li>have the same dimension vector as the unit
     *   <li>share at least one quantityKind with unit
     * </ol>
     *
     * and they are ascending sorted by dissimilarity in magnitude to the magnitude of the specified
     * unit, i.e. the first unit returned is the closest in magnitude.
     *
     * <p>If two resulting units have the same magnitude difference from the specified one, the
     * following comparisons are made consecutively until a difference is found:
     *
     * <ol>
     *   <li>the base unit of the specified system is ranked first
     *   <li>conversion offset closer to the one of the specified unit is ranked first
     *   <li>the unscaled unit is ranked first
     *   <li>the unit that has a symbol is ranked first
     *   <li>the unit with more quantityKinds is ranked first
     *   <li>the units are ranked by their IRIs lexicographically
     * </ol>
     *
     * that is a base unit of the system is ranked first. If none or both are base units, the one
     * with a conversion offset closer to the specified unit's conversion offset is ranked first.
     *
     * @param unit
     * @param systemOfUnits
     * @return
     */
    public static List<Unit> correspondingUnitsInSystem(Unit unit, SystemOfUnits systemOfUnits) {
        if (systemOfUnits.allowsUnit(unit)) {
            return List.of(unit);
        }
        List<Unit> elegible =
                Qudt.getUnitsMap().values().stream()
                        .filter(u -> systemOfUnits.allowsUnit(u))
                        .filter(u -> u.getDimensionVectorIri().equals(unit.getDimensionVectorIri()))
                        .filter(u -> !u.equals(unit))
                        .collect(Collectors.toList());
        if (elegible.size() == 1) {
            return elegible;
        }
        List<Unit> candidates = new ArrayList(elegible);
        // get the unit that is closest in magnitude (conversionFactor)
        // recursively check for factor units
        candidates = new ArrayList(elegible);
        candidates.removeIf(
                u ->
                        !u.getQuantityKinds().stream()
                                .anyMatch(q -> unit.getQuantityKinds().contains(q)));
        if (candidates.size() == 1) {
            return candidates;
        }
        candidates.sort(
                (Unit l, Unit r) -> {
                    double scaleDiffL = Math.abs(scaleDifference(l, unit));
                    double scaleDiffR = Math.abs(scaleDifference(r, unit));
                    double diff = Math.signum(scaleDiffL - scaleDiffR);
                    if (diff != 0) {
                        return (int) diff;
                    }
                    // tie breaker: base unit ranked before non-base unit
                    int cmp =
                            Boolean.compare(
                                    systemOfUnits.hasBaseUnit(r), systemOfUnits.hasBaseUnit(l));
                    if (cmp != 0) {
                        return cmp;
                    }
                    // tie breaker: closer offset
                    double offsetDiffL = Math.abs(offsetDifference(l, unit));
                    double offsetDiffR = Math.abs(offsetDifference(r, unit));
                    cmp = (int) Math.signum(offsetDiffL - offsetDiffR);
                    if (cmp != 0) {
                        return cmp;
                    }
                    // tie breaker: perfer unit that is not scaled
                    cmp = Boolean.compare(l.isScaled(), r.isScaled());
                    if (cmp != 0) {
                        return cmp;
                    }
                    // tie breaker prefer the unit that has a symbol (it's more likely to be
                    // commonly used):
                    cmp = Boolean.compare(r.getSymbol().isPresent(), l.getSymbol().isPresent());
                    if (cmp != 0) {
                        return cmp;
                    }
                    // tie breaker: prefer unit with more quantity kinds (it's less specific)
                    cmp = Integer.compare(l.getQuantityKinds().size(), r.getQuantityKinds().size());
                    if (cmp != 0) {
                        return cmp;
                    }
                    // tie breaker: lexicographically compare iris.
                    return l.getIri().compareTo(r.getIri());
                });
        return candidates;
    }

    private static double scaleDifference(Unit u1, Unit u2) {
        BigDecimal u1Log10 =
                Log10BigDecimal.log10(u1.getConversionMultiplier().orElse(BigDecimal.ONE));
        BigDecimal u2Log10 =
                Log10BigDecimal.log10(u2.getConversionMultiplier().orElse(BigDecimal.ONE));
        return u1Log10.doubleValue() - u2Log10.doubleValue();
    }

    private static double offsetDifference(Unit u1, Unit u2) {
        BigDecimal u1Log10 = u1.getConversionOffset().orElse(BigDecimal.ZERO).abs();
        if (u1Log10.compareTo(BigDecimal.ZERO) > 0) {
            u1Log10 = Log10BigDecimal.log10(u1Log10);
        }
        BigDecimal u2Log10 = u2.getConversionOffset().orElse(BigDecimal.ZERO).abs();
        if (u2Log10.compareTo(BigDecimal.ZERO) > 0) {
            u2Log10 = Log10BigDecimal.log10(u2Log10);
        }
        return u1Log10.doubleValue() - u2Log10.doubleValue();
    }

    public static void addQuantityKind(QuantityKind quantityKind) {
        quantityKinds.put(quantityKind.getIri(), quantityKind);
    }

    public static void addUnit(Unit unit) {
        units.put(unit.getIri(), unit);
    }
}
