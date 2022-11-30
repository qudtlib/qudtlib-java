package io.github.qudtlib.model;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

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
    private final String currencyCode;
    private final Integer currencyNumber;

    public Unit(
            String iri,
            String prefixIri,
            String scalingOfIri,
            String dimensionVectorIri,
            BigDecimal conversionMultiplier,
            BigDecimal conversionOffset,
            Set<String> quantityKindIris,
            String symbol,
            Set<LangString> labels,
            String currencyCode,
            Integer currencyNumber) {
        this.iri = iri;
        this.prefixIri = prefixIri;
        this.scalingOfIri = scalingOfIri;
        this.dimensionVectorIri = dimensionVectorIri;
        this.conversionMultiplier = conversionMultiplier;
        this.conversionOffset = conversionOffset;
        this.quantityKindIris = new HashSet<>(quantityKindIris);
        this.symbol = symbol;
        this.labels = labels;
        this.currencyCode = currencyCode;
        this.currencyNumber = currencyNumber;
    }

    public Unit(
            String iri,
            String prefixIri,
            String scalingOfIri,
            String dimensionVectorIri,
            BigDecimal conversionMultiplier,
            BigDecimal conversionOffset,
            String symbol,
            String currencyCode,
            Integer currencyNumber) {
        this.iri = iri;
        this.prefixIri = prefixIri;
        this.scalingOfIri = scalingOfIri;
        this.dimensionVectorIri = dimensionVectorIri;
        this.conversionMultiplier = conversionMultiplier;
        this.conversionOffset = conversionOffset;
        this.quantityKindIris = new HashSet<>();
        this.symbol = symbol;
        this.labels = new HashSet<>();
        this.currencyCode = currencyCode;
        this.currencyNumber = currencyNumber;
    }

    static boolean isUnitless(Unit unit) {
        return unit.getIri().equals("http://qudt.org/vocab/unit/UNITLESS");
    }

    public QuantityValue convertToQuantityValue(BigDecimal value, Unit toUnit) {
        return new QuantityValue(convert(value, toUnit), toUnit);
    }

    public BigDecimal convert(BigDecimal value, Unit toUnit)
            throws InconvertibleQuantitiesException {
        Objects.requireNonNull(value);
        Objects.requireNonNull(toUnit);
        if (this.equals(toUnit)) {
            return value;
        }
        if (isUnitless(this) || isUnitless(toUnit)) {
            return value;
        }
        if (!isConvertible(toUnit)) {
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
        if (this.conversionOffsetDiffers(toUnit)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot convert from %s to %s just by multiplication as their conversion offsets differ",
                            this, toUnit));
        }
        BigDecimal fromMultiplier = this.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal toMultiplier = toUnit.getConversionMultiplier().orElse(BigDecimal.ONE);
        return fromMultiplier.divide(toMultiplier, MathContext.DECIMAL128);
    }

    public boolean conversionOffsetDiffers(Unit other) {
        if (this.hasNonzeroConversionOffset() && other.hasNonzeroConversionOffset()) {
            return this.conversionOffset.compareTo(other.conversionOffset) != 0;
        }
        return false;
    }

    /**
     * Returns true iff this unit has a non-zero conversion offset.
     *
     * @return
     */
    public boolean hasNonzeroConversionOffset() {
        return this.conversionOffset != null
                && this.conversionOffset.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean isConvertible(Unit toUnit) {
        Objects.requireNonNull(toUnit);
        Objects.requireNonNull(this.dimensionVectorIri);
        return this.dimensionVectorIri.equals(toUnit.dimensionVectorIri);
    }

    public boolean matches(Collection<Map.Entry<String, Integer>> factorUnitSpec) {
        return matches(FactorUnits.ofFactorUnitSpec((factorUnitSpec)));
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
        return matches(FactorUnits.ofFactorUnitSpec(factorUnitSpec));
    }

    /**
     * Checks if this unit matches the specified FactorUnitSelection, i.e. if it is made up of the
     * specified factor units.
     *
     * <p>For example, the unit Nm (Newton Meter) would match a factor Unit selection containing
     * only the still unmatched selectors of (N^1? m^1?), as well as the selection containing
     *
     * @param factorUnits the selection criteria
     * @return true if the unit matches the criteria
     */
    public boolean matches(FactorUnits factorUnits) {
        FactorUnits thisNormalized = this.normalize();
        FactorUnits selectionNormalized = factorUnits.normalize();
        return thisNormalized.equals(selectionNormalized);
    }

    public boolean hasFactorUnits() {
        return this.factorUnits != null && !this.factorUnits.isEmpty();
    }

    public boolean isScaled() {
        return this.scalingOfIri != null;
    }

    /**
     * Returns this unit as a set of exponent-reduced factors, unless they are two factors that
     * cancel each other out, in which case return the unit as a factor unit with exponent 1. For
     * example, Steradian is m²/m² and will therefore return SR.
     */
    public FactorUnits normalize() {
        if (this.hasFactorUnits()) {
            FactorUnits ret =
                    this.factorUnits.stream()
                            .map(fu -> fu.normalize())
                            .reduce((prev, cur) -> cur.combineWith(prev))
                            .get();
            if (ret.isRatioOfSameUnits()) {
                // we don't want to reduce units like M²/M², as such units then match any other unit
                // if they are
                // compared by the normalization result
                return FactorUnits.ofUnit(this);
            }
            return ret.reduceExponents();
        } else if (this.isScaled()) {
            return this.scalingOf.normalize().scale(this.getConversionMultiplier(this.scalingOf));
        }
        return FactorUnits.ofUnit(this);
    }

    public List<FactorUnit> getLeafFactorUnitsWithCumulativeExponents() {
        return this.factorUnits == null || this.factorUnits.isEmpty()
                ? List.of(new FactorUnit(this, 1))
                : factorUnits.stream()
                        .flatMap(f -> f.getLeafFactorUnitsWithCumulativeExponents().stream())
                        .collect(Collectors.toList());
    }

    public List<List<FactorUnit>> getAllPossibleFactorUnitCombinations() {
        if (!this.hasFactorUnits() || this.factorUnits.isEmpty()) {
            if (this.isScaled()) {
                return this.scalingOf.getAllPossibleFactorUnitCombinations();
            }
            return List.of(List.of(FactorUnit.ofUnit(this)));
        }
        List<List<FactorUnit>> result =
                FactorUnit.getAllPossibleFactorUnitCombinations(this.factorUnits);
        List<FactorUnit> thisAsResult = List.of(FactorUnit.ofUnit(this));
        if (!result.contains(thisAsResult)) {
            result.add(thisAsResult);
        }
        return result;
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

    public Optional<String> getCurrencyCode() {
        return Optional.ofNullable(currencyCode);
    }

    public Optional<Integer> getCurrencyNumber() {
        return Optional.ofNullable(currencyNumber);
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
        Objects.requireNonNull(prefix);
        this.prefix = prefix;
    }

    void setScalingOf(Unit scalingOf) {
        Objects.requireNonNull(scalingOf);
        this.scalingOf = scalingOf;
    }

    void addLabel(LangString langString) {
        Objects.requireNonNull(langString);
        this.labels.add(langString);
    }

    void addQuantityKind(String quantityKind) {
        Objects.requireNonNull(
                quantityKind,
                String.format("Property referenced by %s but not found in model", this.toString()));
        this.quantityKindIris.add(quantityKind);
    }

    void addQuantityKind(QuantityKind quantityKind) {
        Objects.requireNonNull(
                quantityKind,
                String.format("Property referenced by %s but not found in model", this.toString()));
        if (this.quantityKinds == null) {
            this.quantityKinds = new HashSet<>();
        }
        this.quantityKinds.add(quantityKind);
    }

    void addFactorUnit(FactorUnit factorUnit) {
        Objects.requireNonNull(
                factorUnit,
                String.format("Property referenced by %s but not found in model", this.toString()));
        if (this.factorUnits == null) {
            this.factorUnits = new ArrayList<>();
        }
        this.factorUnits.add(factorUnit);
    }

    private boolean findInBasesRecursively(Unit toFind) {
        if (!this.isScaled()) {
            return this.equals(toFind);
        }
        return this.getScalingOf()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "No base unit found for %s - this is a bug", this)))
                .findInBasesRecursively(toFind);
    }

    public boolean isSameScaleAs(Unit other) {
        if (this.equals(other)) {
            return true;
        }
        if (this.getScalingOfIri()
                .map(s -> s.equals(other.getScalingOfIri().orElse(null)))
                .orElse(false)) {
            return true;
        }
        return this.findInBasesRecursively(other) || other.findInBasesRecursively(this);
    }
}
