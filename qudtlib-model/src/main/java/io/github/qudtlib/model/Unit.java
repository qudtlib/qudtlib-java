package io.github.qudtlib.model;

import static io.github.qudtlib.nodedef.Builder.buildList;
import static io.github.qudtlib.nodedef.Builder.buildSet;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
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
public class Unit extends SelfSmuggler {

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    static Definition definition(Unit product) {
        return new Definition(product);
    }

    public static class Definition extends NodeDefinitionBase<String, Unit> {

        private String iri;
        private Builder<Prefix> prefix;
        private BigDecimal conversionMultiplier;
        private BigDecimal conversionOffset;
        private Set<Builder<QuantityKind>> quantityKinds = new HashSet<>();
        private String symbol;
        private Set<LangString> labels = new HashSet<>();
        private Builder<Unit> scalingOf;
        private String dimensionVectorIri;
        private List<Builder<FactorUnit>> factorUnits = new ArrayList<>();
        private String currencyCode;
        private Integer currencyNumber;
        private Set<Builder<SystemOfUnits>> unitOfSystems = new HashSet<>();

        Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        Definition(Unit product) {
            super(product.getIri(), product);
            this.iri = product.iri;
        }

        public Definition conversionMultiplier(BigDecimal conversionMultiplier) {
            this.conversionMultiplier = conversionMultiplier;
            return this;
        }

        public Definition conversionOffset(BigDecimal conversionOffset) {
            this.conversionOffset = conversionOffset;
            return this;
        }

        public Definition symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        Definition addLabel(String label, String languageTag) {
            if (label != null) {
                return this.addLabel(new LangString(label, languageTag));
            }
            return this;
        }

        public Definition addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(l));
            return this;
        }

        Definition addLabels(Collection<LangString> labels) {
            this.labels.addAll(labels);
            return this;
        }

        public Definition dimensionVectorIri(String dimensionVectorIri) {
            this.dimensionVectorIri = dimensionVectorIri;
            return this;
        }

        public Definition addFactorUnit(FactorUnit.Builder factorUnit) {
            doIfPresent(factorUnit, f -> this.factorUnits.add(f));
            return this;
        }

        Definition addFactorUnit(FactorUnit factorUnit) {
            doIfPresent(factorUnit, f -> this.factorUnits.add(FactorUnit.builder(f)));
            return this;
        }

        public Definition currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Definition currencyNumber(Integer currencyNumber) {
            this.currencyNumber = currencyNumber;
            return this;
        }

        public Definition addUnitOfSystem(Builder<SystemOfUnits> systemOfUnits) {
            doIfPresent(systemOfUnits, s -> this.unitOfSystems.add(systemOfUnits));
            return this;
        }

        public Definition prefix(Builder<Prefix> prefix) {
            this.prefix = prefix;
            return this;
        }

        public Definition scalingOf(Builder<Unit> scalingOf) {
            this.scalingOf = scalingOf;
            return this;
        }

        public Definition addQuantityKind(Builder<QuantityKind> quantityKind) {
            doIfPresent(quantityKind, q -> this.quantityKinds.add(quantityKind));
            return this;
        }

        public Unit doBuild() {
            return new Unit(this);
        }
    }

    private final String iri;
    private final Prefix prefix;
    private final BigDecimal conversionMultiplier;
    private final BigDecimal conversionOffset;
    private final Set<QuantityKind> quantityKinds;
    private final String symbol;
    private final LangStrings labels;
    private final Unit scalingOf;
    private final String dimensionVectorIri;
    private final List<FactorUnit> factorUnits;
    private final String currencyCode;
    private final Integer currencyNumber;
    private final Set<SystemOfUnits> unitOfSystems;

    private Unit(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.labels);
        Objects.requireNonNull(definition.factorUnits);
        Objects.requireNonNull(definition.quantityKinds);
        if (definition.dimensionVectorIri == null) {
            definition.dimensionVectorIri = "missing:dimensionvector:iri";
            System.err.println("warning: no dimension vector present for unit " + definition.iri);
        }
        Objects.requireNonNull(definition.dimensionVectorIri);
        this.iri = definition.iri;
        this.dimensionVectorIri = definition.dimensionVectorIri;
        this.conversionMultiplier = definition.conversionMultiplier;
        this.conversionOffset = definition.conversionOffset;
        this.symbol = definition.symbol;
        this.currencyCode = definition.currencyCode;
        this.currencyNumber = definition.currencyNumber;
        this.labels = new LangStrings(definition.labels);
        this.prefix = definition.prefix == null ? null : definition.prefix.build();
        this.scalingOf = definition.scalingOf == null ? null : definition.scalingOf.build();
        this.quantityKinds = buildSet(definition.quantityKinds);
        this.factorUnits = buildList(definition.factorUnits);
        this.unitOfSystems = buildSet(definition.unitOfSystems);
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

    /** @return true iff this unit has a non-zero conversion offset. */
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
        return this.scalingOf != null;
    }

    /**
     * Returns this unit as a set of exponent-reduced factors, unless they are two factors that
     * cancel each other out, in which case return the unit as a factor unit with exponent 1. For
     * example, Steradian is m²/m² and will therefore return SR.
     *
     * @return a FactorUnits with the normalized and scaled factors.
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
                ? List.of(FactorUnit.ofUnit(this))
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

    public Optional<String> getDimensionVectorIri() {
        return Optional.ofNullable(dimensionVectorIri);
    }

    public Optional<BigDecimal> getConversionMultiplier() {
        return Optional.ofNullable(conversionMultiplier);
    }

    public Optional<BigDecimal> getConversionOffset() {
        return Optional.ofNullable(conversionOffset);
    }

    public Optional<String> getSymbol() {
        return Optional.ofNullable(symbol);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        return labels.getLangStringForLanguageTag(languageTag, null, true);
    }

    public boolean hasLabel(String label) {
        return labels.containsString(label);
    }

    public Optional<Prefix> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    public Optional<Unit> getScalingOf() {
        return Optional.ofNullable(scalingOf);
    }

    public Set<QuantityKind> getQuantityKinds() {
        return quantityKinds;
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

    public Set<SystemOfUnits> getUnitOfSystems() {
        return unitOfSystems;
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
        if (this.getScalingOf()
                .map(s -> s.equals(other.getScalingOf().orElse(null)))
                .orElse(false)) {
            return true;
        }
        return this.findInBasesRecursively(other) || other.findInBasesRecursively(this);
    }
}
