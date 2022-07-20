package io.github.qudtlib;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.model.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
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
 *
 * // finding unit for factors: m, kg, and s^-2:
 * Set<Unit> myUnits =
 *            Qudt.derivedUnit(
 *                    Qudt.Units.M, 1,
 *                    Qudt.Units.KiloGM, 1,
 *                    Qudt.Units.SEC, -2);
 *
 * // finding factors of Newton:
 * List<FactorUnit> myFactorUnits = Qudt.Units.N.getFactorUnits();
 *
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
    public static final String quantityKindBaseIri = "http://qudt.org/vocab/quantitykind/";
    public static final String unitBaseIri = "http://qudt.org/vocab/unit/";
    public static final String prefixBaseIri = "http://qudt.org/vocab/prefix/";
    private static final Map<String, Unit> units;
    private static final Map<String, QuantityKind> quantityKinds;
    private static final Map<String, Prefix> prefixes;
    private static final BigDecimal BD_1000 = new BigDecimal("1000");

    /*
     * The constants for units, quantity kinds and prefixes are kept in separate package-protected classes as they are
     * quite numerous and would clutter this class too much. They are made available
     */
    public abstract static class Units extends io.github.qudtlib.model.Units {}

    public abstract static class QuantityKinds extends io.github.qudtlib.model.QuantityKinds {}

    public abstract static class Prefixes extends io.github.qudtlib.model.Prefixes {}

    /* Use the Initializer to load and wire all prefixes, units and quantityKinds. */
    static {
        Initializer initializer = null;
        try {
            Class<?> type = Class.forName("io.github.qudtlib.model.InitializerImpl");
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
            prefixes = initializer.loadPrefixes();
            units = initializer.loadUnits();
            quantityKinds = initializer.loadQuantityKinds();
            initializer.loadFactorUnits(units);
            initializer.connectObjects(units, quantityKinds, prefixes);
        } else {
            prefixes = new HashMap<>();
            units = new HashMap<>();
            quantityKinds = new HashMap<>();
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
    public static Unit unitFromLocalname(String localname) {
        return unit(unitIriFromLocalname(localname));
    }

    /**
     * Returns the first unit found whose label matches the specified label after replacing any
     * underscore with space and ignoring case (US locale). If more intricate matching is needed,
     * clients can use <code>{@link #allUnits()}.stream().filter(...)</code>.
     *
     * @param label the matched label
     * @return the first unit found
     */
    public static Unit unitFromLabel(String label) {
        LabelMatcher labelMatcher = new LabelMatcher(label);
        return units.values().stream()
                .filter(u -> u.getLabels().stream().anyMatch(labelMatcher::matches))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No unit found with label " + label));
    }

    /**
     * Returns the {@link Unit} identified the specified IRI. For example, <code>
     * unit("http://qudt.org/vocab/unit/N-PER-M2")</code> yields {@code Qudt.Units.N__PER__M2};
     *
     * @param iri the requested unit IRI
     * @return the unit
     * @throws NotFoundException if no such unit is found.
     */
    public static Unit unit(String iri) {
        Unit unit = units.get(iri);
        if (unit == null) {
            throw new NotFoundException("Unit not found: " + iri);
        }
        return unit;
    }

    /**
     * Returns a unit IRI with the specified localname (even if no such unit exists in the model).
     *
     * @param localname the local name of the IRI that identifies the requested unit.
     * @return the full IRI, possibly identifying a unit
     */
    public static String unitIriFromLocalname(String localname) {
        return unitBaseIri + localname;
    }

    public static Unit scaledUnit(String prefixLabel, String baseUnitLabel) {
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
    public static Unit scaledUnit(Prefix prefix, Unit baseUnit) {
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
        if (unit.getScalingOfIri().isEmpty()) {
            return unit;
        }
        return unit(unit.getScalingOfIri().get());
    }

    /**
     * Returns the list of {@link FactorUnit}s of the unit identified by the specified unit IRI.
     *
     * @param unitIri the IRI of the unit
     * @return the factors of the unit or an empty list if the unit is not a derived unit
     * @throws NotFoundException if the unit IRI does not identify a unit in the model
     */
    public static List<FactorUnit> factorUnits(String unitIri) {
        return factorUnits(unit(unitIri));
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
    public static List<FactorUnit> unscaleFactorUnits(List<FactorUnit> factorUnits) {
        return factorUnits.stream()
                .map(uf -> new FactorUnit(unscale(uf.getUnit()), uf.getExponent()))
                .collect(toList());
    }

    /**
     * Obtains a unit from factor units.
     *
     * <p>For example,
     *
     * <pre>{@code
     * Qudt.derivedUnit(
     *                     Qudt.Units.M, 1,
     *                     Qudt.Units.KiloGM, 1,
     *                     Qudt.Units.SEC, -2);
     * }</pre>
     *
     * will yield a Set containing the Newton Unit ({@code Qudt.Units.N})
     *
     * @param factorUnits a map containing unit to exponent entries.
     * @return the derived units that match the given factor units
     */
    public static Set<Unit> derivedUnit(List<Map.Entry<Unit, Integer>> factorUnits) {
        Object[] arr = new Object[factorUnits.size() * 2];
        return derivedUnitFromFactors(
                factorUnits.stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    /**
     * Obtains a unit from factor units.
     *
     * @see #derivedUnit(List)
     * @param factorUnits the factor units
     * @return the derived unit that match the given factor units
     */
    public static Set<Unit> derivedUnitFromFactorUnits(List<FactorUnit> factorUnits) {
        Object[] arr = new Object[factorUnits.size() * 2];
        return derivedUnitFromFactors(
                factorUnits.stream()
                        .flatMap(e -> Stream.of(e.getUnit(), e.getExponent()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    /**
     * Vararg method, must be an even number of arguments, always alternating types of Unit|String
     * and Integer.
     *
     * @param factorUnitSpecs alternating Unit|String (representing a unit IRI) and Integer (the
     *     exponent)
     * @return the units that match
     */
    static Set<Unit> derivedUnitFromFactors(final Object... factorUnitSpecs) {
        if (factorUnitSpecs.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of arguments is required");
        }
        if (factorUnitSpecs.length > 14) {
            throw new IllegalArgumentException(
                    "No more than 14 arguments (7 factor units) supported");
        }
        List<Unit> matchingUnits =
                units.values().stream()
                        .filter(d -> d.matches(factorUnitSpecs))
                        .collect(Collectors.toList());
        if (matchingUnits.isEmpty()) {
            throw new NotFoundException(
                    "No derived unit found for factors " + Arrays.toString(factorUnitSpecs));
        }
        return new HashSet<>(matchingUnits);
    }

    /**
     * Returns the specifed {@code unit} scaled with the specified {@code prefix}.
     *
     * @param unit the unit to scale
     * @param prefix the prefix to scale by
     * @return the scaled unit
     * @throws NotFoundException if the result is not found in the model
     */
    public static Unit scale(Unit unit, Prefix prefix) {
        Optional<Unit> scaled =
                units.values().stream()
                        .filter(
                                u ->
                                        unit.getIri().equals(u.getScalingOfIri().orElse(null))
                                                && prefix.getIri()
                                                        .equals(u.getPrefixIri().orElse(null)))
                        .findFirst();
        return scaled.orElseThrow(
                () ->
                        new NotFoundException(
                                String.format(
                                        "Qudt does not contain the unit %s, scaled with prefix %s",
                                        unit, prefix)));
    }

    /**
     * Returns the base unit of the specified {@code unit} along with the scale factor needed to
     * convert values from the base unit to the specified unit.
     *
     * @param unit the unit to scale to its base
     * @return a Map.Entry with the base unit and the required scale factor
     */
    public static Map.Entry<Unit, BigDecimal> scaleToBaseUnit(Unit unit) {
        if (unit.getScalingOfIri().isPresent() && unit.getPrefixIri().isPresent()) {
            Unit base = unit(unit.getScalingOfIri().get());
            Prefix prefx = prefix(unit.getPrefixIri().get());
            if (Units.GM.getIri().equals(base.getIri())) {
                return Map.entry(Units.KiloGM, prefx.getMultiplier().multiply(BD_1000));
            }
            return Map.entry(base, prefx.getMultiplier());
        }
        if (Units.GM.getIri().equals(unit.getIri())) {
            return Map.entry(Units.KiloGM, BD_1000);
        }
        return Map.entry(unit, BigDecimal.ONE);
    }

    /**
     * Find the {@link Unit} derived from the specified {@code baseUnit} and {@code exponent}.
     *
     * @param baseUnit the base unit
     * @param exponent the exponent
     * @return the unit
     * @throws NotFoundException if no such unit is found in the model
     */
    public static Set<Unit> derivedUnit(Unit baseUnit, int exponent) {
        return derivedUnitFromFactors(baseUnit, exponent);
    }

    /**
     * Find the {@link Unit} derived from the specified {@code baseUnitIri} and {@code exponent}.
     *
     * @param baseUnitIri the base unit
     * @param exponent the exponent
     * @return the unit
     * @throws NotFoundException if no such unit is found in the model or the baseUnitIri does not
     *     identify a unit
     */
    public static Set<Unit> derivedUnit(String baseUnitIri, int exponent) {
        return derivedUnitFromFactors(unit(baseUnitIri), exponent);
    }

    public static Set<Unit> derivedUnit(
            Unit baseUnit1, int exponent1, Unit baseUnit2, int exponent2) {
        return derivedUnitFromFactors(baseUnit1, exponent1, baseUnit2, exponent2);
    }

    public static Set<Unit> derivedUnit(
            Unit baseUnit1,
            int exponent1,
            Unit baseUnit2,
            int exponent2,
            Unit baseUnit3,
            int exponent3) {
        return derivedUnitFromFactors(
                baseUnit1, exponent1, baseUnit2, exponent2, baseUnit3, exponent3);
    }

    public static Set<Unit> derivedUnit(
            String baseUnitIri1,
            int exponent1,
            String baseUnitIri2,
            int exponent2,
            String baseUnitIri3,
            int exponent3) {
        return derivedUnitFromFactors(
                unit(baseUnitIri1),
                exponent1,
                unit(baseUnitIri2),
                exponent2,
                unit(baseUnitIri3),
                exponent3);
    }

    public static Set<Unit> derivedUnit(
            Unit baseUnit1,
            int exponent1,
            Unit baseUnit2,
            int exponent2,
            Unit baseUnit3,
            int exponent3,
            Unit baseUnit4,
            int exponent4) {
        return derivedUnitFromFactors(
                baseUnit1, exponent1, baseUnit2, exponent2, baseUnit3, exponent3, baseUnit4,
                exponent4);
    }

    public static Set<Unit> derivedUnit(
            String baseUnitIri1,
            int exponent1,
            String baseUnitIri2,
            int exponent2,
            String baseUnitIri3,
            int exponent3,
            String baseUnitIri4,
            int exponent4) {
        return derivedUnitFromFactors(
                unit(baseUnitIri1),
                exponent1,
                unit(baseUnitIri2),
                exponent2,
                unit(baseUnitIri3),
                exponent3,
                unit(baseUnitIri4),
                exponent4);
    }

    public static Set<Unit> derivedUnit(
            Unit baseUnit1,
            int exponent1,
            Unit baseUnit2,
            int exponent2,
            Unit baseUnit3,
            int exponent3,
            Unit baseUnit4,
            int exponent4,
            Unit baseUnit5,
            int exponent5) {
        return derivedUnitFromFactors(
                baseUnit1, exponent1, baseUnit2, exponent2, baseUnit3, exponent3, baseUnit4,
                exponent4, baseUnit5, exponent5);
    }

    public static Set<Unit> derivedUnit(
            String baseUnitIri1,
            int exponent1,
            String baseUnitIri2,
            int exponent2,
            String baseUnitIri3,
            int exponent3,
            String baseUnitIri4,
            int exponent4,
            String baseUnitIri5,
            int exponent5) {
        return derivedUnitFromFactors(
                unit(baseUnitIri1),
                exponent1,
                unit(baseUnitIri2),
                exponent2,
                unit(baseUnitIri3),
                exponent3,
                unit(baseUnitIri4),
                exponent4,
                unit(baseUnitIri5),
                exponent5);
    }

    public static Set<Unit> derivedUnit(
            Unit baseUnit1,
            int exponent1,
            Unit baseUnit2,
            int exponent2,
            Unit baseUnit3,
            int exponent3,
            Unit baseUnit4,
            int exponent4,
            Unit baseUnit5,
            int exponent5,
            Unit baseUnit6,
            int exponent6) {
        return derivedUnitFromFactors(
                baseUnit1, exponent1, baseUnit2, exponent2, baseUnit3, exponent3, baseUnit4,
                exponent4, baseUnit5, exponent5, baseUnit6, exponent6);
    }

    public static Set<Unit> derivedUnit(
            String baseUnitIri1,
            int exponent1,
            String baseUnitIri2,
            int exponent2,
            String baseUnitIri3,
            int exponent3,
            String baseUnitIri4,
            int exponent4,
            String baseUnitIri5,
            int exponent5,
            String baseUnitIri6,
            int exponent6) {
        return derivedUnitFromFactors(
                unit(baseUnitIri1),
                exponent1,
                unit(baseUnitIri2),
                exponent2,
                unit(baseUnitIri3),
                exponent3,
                unit(baseUnitIri4),
                exponent4,
                unit(baseUnitIri5),
                exponent5,
                unit(baseUnitIri6),
                exponent6);
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
    public static QuantityKind quantityKindFromLocalname(String localname) {
        return quantityKind(quantityKindIriFromLocalname(localname));
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
    public static QuantityKind quantityKind(String iri) {
        QuantityKind quantityKind = quantityKinds.get(iri);
        if (quantityKind == null) {
            throw new NotFoundException("QuantityKind not found: " + iri);
        }
        return quantityKind;
    }

    /**
     * Returns the {@link QuantityKind}s associated with the specified {@link Unit}.
     *
     * @param unit the unit
     * @return the quantity kinds
     */
    public static Set<QuantityKind> quantityKinds(Unit unit) {
        return unit.getQuantityKindIris().stream()
                .map(Qudt::quantityKind)
                .collect(Collectors.toSet());
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
                            .map(Qudt::quantityKind)
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
        return quantityKindBaseIri + localname;
    }

    /**
     * Returns a prefix IRI with the specified localname (even if no such prefix exists in the
     * model).
     *
     * @param localname the local name of the IRI that identifies the requested prefix.
     * @return the full IRI, possibly identifying a prefix
     */
    public static String prefixIriFromLocalname(String localname) {
        return prefixBaseIri + localname;
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
    public static Prefix prefixFromLocalname(String localname) {
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
    public static Prefix prefix(String iri) {
        Prefix prefix = prefixes.get(iri);
        if (prefix == null) {
            throw new NotFoundException("Prefix not found: " + iri);
        }
        return prefix;
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
        return new QuantityValue(value, unit(unitIri));
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
    public static QuantityValue convert(QuantityValue from, Unit toUnit) {
        return convert(from.getValue(), from.getUnit(), toUnit);
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
            throws InconvertibleQuantitiesException {
        Unit toUnit = unit(toUnitIri);
        return convert(from.getValue(), from.getUnit(), toUnit);
    }

    /**
     * Convert the specified <code>fromValue</code>, interpreted to be in the unit identified by
     * <code>fromUnitIri</code> into the unit identified by the specified IRI <code>toUnitIri</code>
     * .
     *
     * @param fromValue the value to convert
     * @param fromUnitIri the IRI of unit the <code>fromValue</code> is in
     * @param toUnitIri the IRI of the target unit
     * @return a new {@link QuantityValue} object holding the result.
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     * @throws NotFoundException if <code>fromUnitIri</code> or <code>toUnitIri</code> does not
     *     identify a unit in the model
     */
    public static QuantityValue convert(
            BigDecimal fromValue, String fromUnitIri, String toUnitIri) {
        return convert(fromValue, unit(fromUnitIri), unit(toUnitIri));
    }

    /**
     * Convert the specified <code>fromValue</code>, interpreted to be in the {@link Unit} <code>
     * fromUnit</code> into the unit <code>toUnit</code>.
     *
     * @param fromValue the value to convert
     * @param fromUnit the unit of the <code>value</code>
     * @param toUnit the target unit
     * @return a new {@link QuantityValue} object holding the result.
     * @throws InconvertibleQuantitiesException if the conversion is not possible
     */
    public static QuantityValue convert(BigDecimal fromValue, Unit fromUnit, Unit toUnit) {
        if (fromUnit.equals(toUnit)) {
            return new QuantityValue(fromValue, toUnit);
        }
        if (Qudt.Units.UNITLESS.equals(fromUnit) || Qudt.Units.UNITLESS.equals(toUnit)) {
            return new QuantityValue(fromValue, toUnit);
        }
        boolean isConvertible = isConvertible(fromUnit, toUnit);
        if (!isConvertible) {
            throw new InconvertibleQuantitiesException(
                    String.format(
                            "Cannot convert from %s to %s", fromUnit.getIri(), toUnit.getIri()));
        }
        BigDecimal fromOffset = fromUnit.getConversionOffset().orElse(BigDecimal.ZERO);
        BigDecimal fromMultiplier = fromUnit.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal toOffset = toUnit.getConversionOffset().orElse(BigDecimal.ZERO);
        BigDecimal toMultiplier = toUnit.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal converted =
                fromValue
                        .add(fromOffset)
                        .multiply(fromMultiplier, MathContext.DECIMAL128)
                        .divide(toMultiplier, MathContext.DECIMAL128)
                        .subtract(toOffset);
        return new QuantityValue(converted, toUnit);
    }

    /**
     * Indicates whether the two specified {@link Unit}s are convertible into each other.
     *
     * @param fromUnit a unit
     * @param toUnit another unit
     * @return <code>true</code> if the units are convertible.
     */
    public static boolean isConvertible(Unit fromUnit, Unit toUnit) {
        Set<String> fromDimensionVectors =
                fromUnit.getQuantityKindIris().stream()
                        .map(Qudt::quantityKind)
                        .map(QuantityKind::getDimensionVector)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toSet());
        Set<String> toDimensionVectors =
                toUnit.getQuantityKindIris().stream()
                        .map(Qudt::quantityKind)
                        .map(QuantityKind::getDimensionVector)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toSet());
        return fromDimensionVectors.stream().anyMatch(toDimensionVectors::contains);
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
}
