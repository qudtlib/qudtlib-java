package io.github.qudtlib.model;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a QUDT Unit.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class Unit {
    private final String iri;
    private final String prefixIri;
    private Prefix prefix;
    private final BigDecimal conversionMultiplier;
    private final BigDecimal conversionOffset;
    private final Set<String> quantityKindIris;
    private Set<QuantityKind> quantityKinds;
    private final String symbol;
    private final Set<LangString> labels;
    private final String scalingOfIri;
    private Unit scalingOf;
    private final String dimensionVectorIri;
    private List<FactorUnit> factorUnits = null;

    public Unit(
            String iri,
            String prefixIri,
            String scalingOfIri,
            String dimensionVectorIri,
            BigDecimal conversionMultiplier,
            BigDecimal conversionOffset,
            Set<String> quantityKindIris,
            String symbol,
            Set<LangString> labels) {
        this.iri = iri;
        this.prefixIri = prefixIri;
        this.scalingOfIri = scalingOfIri;
        this.dimensionVectorIri = dimensionVectorIri;
        this.conversionMultiplier = conversionMultiplier;
        this.conversionOffset = conversionOffset;
        this.quantityKindIris = new HashSet<>(quantityKindIris);
        this.symbol = symbol;
        this.labels = labels;
    }

    public Unit(
            String iri,
            String prefixIri,
            String scalingOfIri,
            String dimensionVectorIri,
            BigDecimal conversionMultiplier,
            BigDecimal conversionOffset,
            String symbol) {
        this.iri = iri;
        this.prefixIri = prefixIri;
        this.scalingOfIri = scalingOfIri;
        this.dimensionVectorIri = dimensionVectorIri;
        this.conversionMultiplier = conversionMultiplier;
        this.conversionOffset = conversionOffset;
        this.quantityKindIris = new HashSet<>();
        this.symbol = symbol;
        this.labels = new HashSet<>();
    }

    static boolean isUnitless(Unit unit) {
        return unit.getIri().equals("http://qudt.org/vocab/unit/UNITLESS");
    }

    public QuantityValue convertToQuantityValue(BigDecimal value, Unit toUnit) {
        return new QuantityValue(convert(value, toUnit), toUnit);
    }

    public BigDecimal convert(BigDecimal value, Unit toUnit) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(toUnit);
        if (this.equals(toUnit)) {
            return value;
        }
        if (isUnitless(this) || isUnitless(toUnit)) {
            return value;
        }
        boolean isConvertible = isConvertible(toUnit);
        if (!isConvertible) {
            throw new InconvertibleQuantitiesException(
                    String.format(
                            "Cannot convert from %s to %s: dimension vectors differ",
                            this.getIri(), toUnit.getIri()));
        }
        BigDecimal fromOffset = this.getConversionOffset().orElse(BigDecimal.ZERO);
        BigDecimal fromMultiplier = this.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal toOffset = toUnit.getConversionOffset().orElse(BigDecimal.ZERO);
        BigDecimal toMultiplier = toUnit.getConversionMultiplier().orElse(BigDecimal.ONE);
        return value.add(fromOffset)
                .multiply(fromMultiplier, MathContext.DECIMAL128)
                .divide(toMultiplier, MathContext.DECIMAL128)
                .subtract(toOffset);
    }

    /**
     * Returns the multiplier required to convert from this unit into <code>toUnit</code>.
     *
     * @param toUnit the unit the resulting multiplier converts to
     * @return the multiplier
     * @throws IllegalArgumentException if either of this or <code>toUnit</code> has a non-null
     *     <code>conversionOffset</code>.
     */
    public BigDecimal getConversionMultiplier(Unit toUnit) {
        if (this.equals(toUnit)) {
            return BigDecimal.ONE;
        }
        if (this.conversionOffset != null || toUnit.conversionOffset != null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot convert from %s to %s just by multiplication, one of them has a conversion offset!",
                            this, toUnit));
        }
        BigDecimal fromMultiplier = this.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal toMultiplier = toUnit.getConversionMultiplier().orElse(BigDecimal.ONE);
        return fromMultiplier.divide(toMultiplier, MathContext.DECIMAL128);
    }

    public boolean isConvertible(Unit toUnit) {
        Objects.requireNonNull(toUnit);
        return this.dimensionVectorIri.equals(toUnit.dimensionVectorIri);
    }

    public boolean matches(Collection<Map.Entry<String, Integer>> factorUnitSpec) {
        Object[] arr = new Object[factorUnitSpec.size() * 2];
        return matches(
                factorUnitSpec.stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    /**
     * Accepts up to 7 pairs of &lt;Unit, Integer&gt; which are interpreted as factor units and
     * respective exponents.
     *
     * @param factorUnitSpec array of up to 7 %lt;Unit, Integer%gt; pairs
     * @return true if the specified unit/exponent combination identifies this unit.
     *     (overspecification is counted as a match)
     */
    public boolean matches(Object... factorUnitSpec) {
        if (factorUnitSpec.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of arguments is required");
        }
        if (factorUnitSpec.length > 14) {
            throw new IllegalArgumentException(
                    "No more than 10 arguments (7 factor units) supported");
        }
        boolean abortSearch = false;
        List<FactorUnitSelector> selectors = new ArrayList<>();
        for (int i = 0; i < factorUnitSpec.length && !abortSearch; i += 2) {
            Unit requestedUnit;
            requestedUnit = ((Unit) factorUnitSpec[i]);
            Integer requestedExponent = (Integer) factorUnitSpec[i + 1];
            selectors.add(new FactorUnitSelector(requestedUnit, requestedExponent));
        }
        Set<FactorUnitSelection> selections = Set.of(new FactorUnitSelection(selectors));
        selections = match(selections, 1, new ArrayDeque<>(), new ScaleFactor());
        if (selections == null || selections.isEmpty()) return false;
        return selections.stream()
                .filter(FactorUnitSelection::isCompleteMatch)
                .anyMatch(sel -> this.isMatched(sel, new ArrayDeque<>()));
    }

    boolean isMatched(FactorUnitSelection selection, Deque<Unit> checkedPath) {
        checkedPath.push(this);
        boolean match = false;
        if (hasFactorUnits()) {
            match = getFactorUnits().stream().allMatch(fu -> fu.isMatched(selection, checkedPath));
        }
        if (!match && isScaled()) {
            match = getScalingOf().map(u -> u.isMatched(selection, checkedPath)).orElse(false);
        }
        checkedPath.pop();
        return match;
    }

    Set<FactorUnitSelection> match(
            Set<FactorUnitSelection> selections,
            int cumulativeExponent,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        Set<FactorUnitSelection> results = new HashSet<>();
        matchedPath.push(this);
        if (this.getScalingOf().isPresent() && getPrefix().isPresent()) {
            results.addAll(
                    this.getScalingOf()
                            .get()
                            .match(
                                    selections,
                                    cumulativeExponent,
                                    matchedPath,
                                    scaleFactor.multiplyBy(
                                            this.getPrefix().get().getMultiplier())));
        }
        if (hasFactorUnits()) {
            for (FactorUnit factorUnit : factorUnits) {
                selections =
                        factorUnit.match(selections, cumulativeExponent, matchedPath, scaleFactor);
            }
        }
        results.addAll(selections);
        matchedPath.pop();
        return results;
    }

    public boolean hasFactorUnits() {
        return this.factorUnits != null && !this.factorUnits.isEmpty();
    }

    public boolean isScaled() {
        return this.scalingOfIri != null;
    }

    public List<FactorUnit> getLeafFactorUnitsWithCumulativeExponents() {
        return this.factorUnits == null
                ? Collections.emptyList()
                : factorUnits.stream()
                        .flatMap(f -> f.getLeafFactorUnitsWithCumulativeExponents().stream())
                        .collect(Collectors.toList());
    }

    public String getIri() {
        return iri;
    }

    public Optional<String> getPrefixIri() {
        return Optional.ofNullable(prefixIri);
    }

    public Optional<String> getScalingOfIri() {
        return Optional.ofNullable(scalingOfIri);
    }

    public Optional<String> getDimensionVectorIri() {
        return Optional.ofNullable(dimensionVectorIri);
    }

    public Optional<BigDecimal> getConversionMultiplier() {
        return Optional.ofNullable(conversionMultiplier);
    }

    public Optional<BigDecimal> getConversionOffset() {
        return Optional.ofNullable(conversionOffset);
    }

    public Set<String> getQuantityKindIris() {
        return Collections.unmodifiableSet(quantityKindIris);
    }

    public Optional<String> getSymbol() {
        return Optional.ofNullable(symbol);
    }

    public Set<LangString> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        if (languageTag == null) {
            return labels.stream().filter(s -> s.getLanguageTag().isEmpty()).findFirst();
        } else {
            return labels.stream()
                    .filter(s -> languageTag.equals(s.getLanguageTag().orElse(null)))
                    .findFirst();
        }
    }

    public boolean hasLabel(String label) {
        return labels.stream().anyMatch(s -> s.getString().equals(label));
    }

    public Optional<Prefix> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    public Optional<Unit> getScalingOf() {
        return Optional.ofNullable(scalingOf);
    }

    public Set<QuantityKind> getQuantityKinds() {
        return Collections.unmodifiableSet(quantityKinds);
    }

    public List<FactorUnit> getFactorUnits() {
        return factorUnits == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(factorUnits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return Objects.equals(iri, unit.iri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri);
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol;
        }
        if (scalingOf != null && scalingOf.getSymbol().isPresent() && prefix != null) {
            return prefix.getSymbol() + scalingOf.getSymbol().get();
        }
        return "unit:" + iri.replaceAll(".+/([^/]+)", "$1");
    }

    void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    void setScalingOf(Unit scalingOf) {
        this.scalingOf = scalingOf;
    }

    void addLabel(LangString langString) {
        this.labels.add(langString);
    }

    void addQuantityKind(String quantityKind) {
        this.quantityKindIris.add(quantityKind);
    }

    void addQuantityKind(QuantityKind quantityKind) {
        if (this.quantityKinds == null) {
            this.quantityKinds = new HashSet<>();
        }
        this.quantityKinds.add(quantityKind);
    }

    void addFactorUnit(FactorUnit factorUnit) {
        if (this.factorUnits == null) {
            this.factorUnits = new ArrayList<>();
        }
        this.factorUnits.add(factorUnit);
    }
}
