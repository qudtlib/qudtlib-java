package io.github.qudtlib.model;

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
    private static final String TEMPERATURE_DIFFERENCE = "TemperatureDifference";

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    public static Definition definition(Unit product) {
        return new Definition(product);
    }

    public static Definition definition(String iriBase, FactorUnits factors) {
        String localName = factors.getLocalname();
        Definition definition = new Definition(iriBase + localName);
        factors.getSymbol().ifPresent(definition::symbol);
        factors.getUcumCode().ifPresent(definition::ucumCode);
        List<FactorUnit> fus = factors.getFactorUnits();
        definition.setFactorUnits(factors);

        fus.stream()
                .map(u -> u.getUnit().getUnitOfSystems())
                .reduce(
                        (a, b) -> {
                            HashSet<SystemOfUnits> set = new HashSet<>(a);
                            set.retainAll(b);
                            return set;
                        })
                .ifPresent(commonSystems -> commonSystems.forEach(definition::addSystemOfUnits));

        definition.dimensionVectorIri(factors.getDimensionVectorIri());

        definition.conversionMultiplier(factors.getConversionMultiplier());

        return definition;
    }

    public static class Definition extends NodeDefinitionBase<String, Unit> {

        private String iri;
        private Builder<Prefix> prefix;
        private BigDecimal conversionMultiplier;
        private BigDecimal conversionOffset;
        private Set<Builder<QuantityKind>> quantityKinds = new HashSet<>();
        private String symbol;
        private Set<String> altSymbols = new HashSet<>();
        private String description;
        private boolean generated = false;
        private String ucumCode;
        private Set<LangString> labels = new HashSet<>();
        private Builder<Unit> scalingOf;
        private String dimensionVectorIri;

        private FactorUnits.Builder factorUnits = FactorUnits.builder();
        private String currencyCode;
        private Integer currencyNumber;

        private Boolean deprecated;

        private Integer dependents;

        private Set<Builder<Unit>> exactMatches = new HashSet<>();
        private Set<Builder<SystemOfUnits>> systemsOfUnits = new HashSet<>();

        protected Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        protected Definition(Unit product) {
            super(product.getIri(), product);
            this.iri = product.iri;
        }

        public <T extends Definition> T conversionMultiplier(BigDecimal conversionMultiplier) {
            this.conversionMultiplier = conversionMultiplier;
            return (T) this;
        }

        public <T extends Definition> T conversionOffset(BigDecimal conversionOffset) {
            this.conversionOffset = conversionOffset;
            return (T) this;
        }

        public <T extends Definition> T symbol(String symbol) {
            this.symbol = symbol;
            return (T) this;
        }

        public <T extends Definition> T dependents(Integer dependents) {
            this.dependents = dependents;
            return (T) this;
        }

        public <T extends Definition> T altSymbol(String symbol) {
            if (symbol != null && !symbol.trim().isEmpty()) {
                this.altSymbols.add(symbol);
            }
            return (T) this;
        }

        public <T extends Definition> T ucumCode(String ucumCode) {
            this.ucumCode = ucumCode;
            return (T) this;
        }

        public <T extends Definition> T generated(boolean generated) {
            this.generated = generated;
            return (T) this;
        }

        public <T extends Definition> T clearLabels() {
            this.labels.clear();
            return (T) this;
        }

        public <T extends Definition> T description(String description) {
            this.description = description;
            return (T) this;
        }

        public <T extends Definition> T addLabel(String label, String languageTag) {
            if (label != null) {
                return this.addLabel(new LangString(label, languageTag));
            }
            return (T) this;
        }

        public <T extends Definition> T addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(l));
            return (T) this;
        }

        public <T extends Definition> T addLabels(Collection<LangString> labels) {
            this.labels.addAll(labels);
            return (T) this;
        }

        public <T extends Definition> T dimensionVectorIri(String dimensionVectorIri) {
            this.dimensionVectorIri = dimensionVectorIri;
            return (T) this;
        }

        public <T extends Definition> T addExactMatch(Builder<Unit> unit) {
            doIfPresent(unit, u -> this.exactMatches.add(u));
            return (T) this;
        }

        public <T extends Definition> T addExactMatch(Unit unit) {
            doIfPresent(unit, u -> this.exactMatches.add(Unit.definition(u)));
            return (T) this;
        }

        public <T extends Definition> T addFactorUnit(FactorUnit.Builder factorUnit) {
            doIfPresent(factorUnit, f -> this.factorUnits.factor(factorUnit));
            return (T) this;
        }

        public <T extends Definition> T addFactorUnit(FactorUnit factorUnit) {
            doIfPresent(factorUnit, f -> this.factorUnits.factor(factorUnit));
            return (T) this;
        }

        public <T extends Definition> T addFactorUnits(Collection<FactorUnit> factorUnits) {
            doIfPresent(
                    factorUnits, f -> factorUnits.stream().forEach(fu -> this.addFactorUnit(fu)));
            return (T) this;
        }

        public <T extends Definition> T setFactorUnits(FactorUnits.Builder factorUnits) {
            this.factorUnits = factorUnits;
            return (T) this;
        }

        public <T extends Definition> T setFactorUnits(FactorUnits factorUnits) {
            this.factorUnits = FactorUnits.builderOf(factorUnits);
            return (T) this;
        }

        public <T extends Definition> T setFactorUnits(Collection<FactorUnit> factorUnits) {
            this.factorUnits = FactorUnits.builder();
            doIfPresent(
                    factorUnits, f -> factorUnits.stream().forEach(fu -> this.addFactorUnit(fu)));
            return (T) this;
        }

        public <T extends Definition> T deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return (T) this;
        }

        public <T extends Definition> T currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return (T) this;
        }

        public <T extends Definition> T currencyNumber(Integer currencyNumber) {
            this.currencyNumber = currencyNumber;
            return (T) this;
        }

        public <T extends Definition> T addSystemOfUnits(Builder<SystemOfUnits> systemOfUnits) {
            doIfPresent(systemOfUnits, s -> this.systemsOfUnits.add(systemOfUnits));
            return (T) this;
        }

        public <T extends Definition> T addSystemOfUnits(SystemOfUnits systemOfUnits) {
            doIfPresent(
                    systemOfUnits,
                    s -> this.systemsOfUnits.add(SystemOfUnits.definition(systemOfUnits)));
            return (T) this;
        }

        public <T extends Definition> T prefix(Builder<Prefix> prefix) {
            this.prefix = prefix;
            return (T) this;
        }

        public <T extends Definition> T prefix(Prefix prefix) {
            this.prefix = Prefix.definition(prefix);
            return (T) this;
        }

        public <T extends Definition> T scalingOf(Builder<Unit> scalingOf) {
            this.scalingOf = scalingOf;
            return (T) this;
        }

        public <T extends Definition> T scalingOf(Unit scalingOf) {
            this.scalingOf = Unit.definition(scalingOf);
            return (T) this;
        }

        public <T extends Definition> T addQuantityKind(Builder<QuantityKind> quantityKind) {
            doIfPresent(quantityKind, q -> this.quantityKinds.add(quantityKind));
            return (T) this;
        }

        public <T extends Definition> T addQuantityKind(QuantityKind quantityKind) {
            doIfPresent(
                    quantityKind,
                    q -> this.quantityKinds.add(QuantityKind.definition(quantityKind)));
            return (T) this;
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
    private final Set<String> altSymbols;
    private final String description;

    private final String ucumCode;
    private final LangStrings labels;
    private final Unit scalingOf;
    private final int dependents;
    private final Set<Unit> exactMatches;
    private final FactorUnits factorUnits;
    private final String currencyCode;
    private final Integer currencyNumber;
    private final Set<SystemOfUnits> unitOfSystems;

    private DimensionVector dimensionVector;

    private final boolean deprecated;
    private final boolean generated;

    protected Unit(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.labels);
        Objects.requireNonNull(definition.factorUnits);
        Objects.requireNonNull(definition.quantityKinds);
        this.iri = definition.iri;
        if (definition.dimensionVectorIri != null) {
            this.dimensionVector = new DimensionVector(definition.dimensionVectorIri);
        }
        this.conversionMultiplier = definition.conversionMultiplier;
        this.conversionOffset = definition.conversionOffset;
        this.symbol = definition.symbol;
        this.altSymbols = definition.altSymbols;
        this.ucumCode = definition.ucumCode;
        this.currencyCode = definition.currencyCode;
        this.currencyNumber = definition.currencyNumber;
        this.labels = new LangStrings(definition.labels);
        this.description = definition.description;
        this.prefix = definition.prefix == null ? null : definition.prefix.build();
        this.scalingOf = definition.scalingOf == null ? null : definition.scalingOf.build();
        this.dependents = definition.dependents == null ? 0 : definition.dependents;
        this.exactMatches = buildSet(definition.exactMatches);
        this.quantityKinds = buildSet(definition.quantityKinds);
        this.unitOfSystems = buildSet(definition.systemsOfUnits);
        this.generated = definition.generated;
        FactorUnits fu = definition.factorUnits.build();
        if (definition.scalingOf != null && fu.hasFactorUnits()) {
            BigDecimal multiplier =
                    this.prefix == null
                            ? definition.conversionMultiplier
                            : this.prefix
                                    .getMultiplier()
                                    .pow(fu.getFactorUnits().get(0).getExponent());
            FactorUnits fuForSclaingOf =
                    FactorUnits.ofFactorUnitSpec(multiplier, this.scalingOf, 1);
            if (!fu.normalize().equals(fuForSclaingOf.normalize())) {
                throw new IllegalArgumentException(
                        String.format(
                                "Unit %s has conflicting definition of factor units (%s) and scalingOf (%s, which implies factor units %s)",
                                this.iri, fu.toString(), this.scalingOf, fuForSclaingOf));
            }
        }
        if (fu.hasFactorUnits()) {
            this.factorUnits = new FactorUnits(fu);
        } else if (this.scalingOf != null) {
            BigDecimal multiplier =
                    this.prefix == null
                            ? definition.conversionMultiplier
                            : this.prefix.getMultiplier();
            this.factorUnits = FactorUnits.ofFactorUnitSpec(multiplier, this.scalingOf, 1);
        } else {
            this.factorUnits = FactorUnits.ofUnit(this);
        }
        this.deprecated = Optional.ofNullable(definition.deprecated).orElse(false);
    }

    static boolean isUnitless(Unit unit) {
        return unit.getIri().equals("http://qudt.org/vocab/unit/UNITLESS");
    }

    public QuantityValue convertToQuantityValue(BigDecimal value, Unit toUnit) {
        return new QuantityValue(convert(value, toUnit), toUnit);
    }

    public BigDecimal convert(BigDecimal value, Unit toUnit)
            throws InconvertibleQuantitiesException {
        return convert(value, toUnit, null);
    }

    /**
     * Convert method allowing for special handling depending on the specified quantity kind.
     * Introduced to ignore the offset when converting a temperature difference.
     *
     * @param value
     * @param toUnit
     * @param quantityKind optional quantity kind for handling edge cases. Pass null for normal
     *     conversion.
     * @return
     * @throws InconvertibleQuantitiesException
     */
    public BigDecimal convert(BigDecimal value, Unit toUnit, QuantityKind quantityKind)
            throws InconvertibleQuantitiesException {
        Objects.requireNonNull(value);
        Objects.requireNonNull(toUnit);
        boolean ignoreOffset = false;
        if (quantityKind != null) {
            if (quantityKind.getIriLocalname().equals(TEMPERATURE_DIFFERENCE)) {
                ignoreOffset = true;
            }
        }
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
        BigDecimal fromOffset =
                ignoreOffset ? BigDecimal.ZERO : this.getConversionOffset().orElse(BigDecimal.ZERO);
        BigDecimal fromMultiplier = this.getConversionMultiplier().orElse(BigDecimal.ONE);
        BigDecimal toOffset =
                ignoreOffset
                        ? BigDecimal.ZERO
                        : toUnit.getConversionOffset().orElse(BigDecimal.ZERO);
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
                            "Cannot convert from %s to %s just by multiplication as their conversion offsets differ (%s vs %s)",
                            this, toUnit, this.conversionOffset, toUnit.conversionOffset));
        }
        Optional<BigDecimal> fromMultiplier = this.getConversionMultiplier();
        Optional<BigDecimal> toMultiplier = toUnit.getConversionMultiplier();
        return fromMultiplier
                .map(
                        from ->
                                toMultiplier
                                        .map(to -> from.divide(to, MathContext.DECIMAL128))
                                        .orElse(null))
                .orElseThrow(
                        () ->
                                new InconvertibleQuantitiesException(
                                        String.format(
                                                "Cannot convert %s(%s) to %s(%s)",
                                                this.getIriAbbreviated(),
                                                this.getConversionMultiplier().isEmpty()
                                                        ? "no multiplier"
                                                        : "has multiplier",
                                                toUnit.getIriAbbreviated(),
                                                toUnit.getConversionMultiplier().isEmpty()
                                                        ? "no multiplier"
                                                        : "has multiplier")));
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
        if (toUnit == null
                || toUnit.getDimensionVectorIri() == null
                || this.getDimensionVectorIri() == null) {
            return false;
        }

        return this.getDimensionVectorIri().equals(toUnit.getDimensionVectorIri());
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
        try {
            FactorUnits thisNormalized = this.normalize();
            FactorUnits selectionNormalized = factorUnits.normalize();
            return thisNormalized.equals(selectionNormalized);
        } catch (InconvertibleQuantitiesException e) {
            return false;
        }
    }

    public boolean hasFactorUnits() {
        if (this.factorUnits == null) {
            // defensive check for calculations during instantiation
            return false;
        }
        return this.factorUnits.hasFactorUnits();
    }

    /**
     * Returns true if this unit is defined to be another unit, such as litre is defined as cubic
     * decimetre.
     */
    public boolean isDefinedAsOtherUnit() {
        return this.factorUnits.isOneOtherUnitWithExponentOne();
    }

    public boolean isScaled() {
        return this.scalingOf != null;
    }

    /**
     * Returns this unit as a set of exponent-reduced factors, unless they are two factors that
     * cancel each other out, in which case return the unit as a factor unit with exponent 1. For
     * example, Steradian is m²/m² and will therefore return SR.
     */
    public FactorUnits normalize() {
        if (this.hasFactorUnits()) {
            return this.factorUnits.normalize();
        } else if (this.isScaled()) {
            return this.scalingOf.normalize().scale(this.getConversionMultiplier(this.scalingOf));
        }
        if (this.factorUnits == null) {
            // defensive branch for use during initialization
            return FactorUnits.ofUnit(this);
        }
        return this.factorUnits;
    }

    public List<FactorUnit> getLeafFactorUnitsWithCumulativeExponents() {
        return this.hasFactorUnits()
                ? factorUnits.getFactorUnits().stream()
                        .flatMap(f -> f.getLeafFactorUnitsWithCumulativeExponents().stream())
                        .collect(Collectors.toList())
                : List.of(FactorUnit.ofUnit(this));
    }

    public List<List<FactorUnit>> getAllPossibleFactorUnitCombinations() {
        if (!this.hasFactorUnits()) {
            if (this.isScaled()) {
                return this.scalingOf.getAllPossibleFactorUnitCombinations();
            }
            return List.of(List.of(FactorUnit.ofUnit(this)));
        }
        List<List<FactorUnit>> result =
                FactorUnit.getAllPossibleFactorUnitCombinations(this.factorUnits.getFactorUnits());
        List<FactorUnit> thisAsResult = List.of(FactorUnit.ofUnit(this));
        if (!result.contains(thisAsResult)) {
            result.add(thisAsResult);
        }
        return result;
    }

    public String getIri() {
        return iri;
    }

    public Optional<DimensionVector> getDimensionVector() {
        if (this.dimensionVector != null) {
            return Optional.of(this.dimensionVector);
        }

        if (this.factorUnits != null && this.factorUnits.hasFactorUnits()) {
            this.dimensionVector = this.factorUnits.getDimensionVector();
            return Optional.of(this.dimensionVector);
        }

        if (this.quantityKinds != null) {
            return this.quantityKinds.stream()
                    .map(QuantityKind::getDimensionVector)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        }

        return Optional.empty();
    }

    public Optional<String> getDimensionVectorIri() {
        return this.getDimensionVector().map(DimensionVector::getDimensionVectorIri);
    }

    public Optional<BigDecimal> getConversionMultiplier() {
        if (this.conversionMultiplier != null) {
            return Optional.of(this.conversionMultiplier);
        }
        if (this.isScaled() && this.prefix != null && this.scalingOf.conversionMultiplier != null) {
            return Optional.of(
                    this.prefix
                            .getMultiplier()
                            .multiply(this.scalingOf.conversionMultiplier, MathContext.DECIMAL128));
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> getConversionOffset() {
        return Optional.ofNullable(conversionOffset);
    }

    public Optional<String> getSymbol() {
        if (this.symbol != null) {
            return Optional.of(symbol);
        }
        if (this.isScaled() && this.prefix != null && this.scalingOf.symbol != null) {
            return Optional.of(this.prefix.getSymbol() + this.scalingOf.getSymbol());
        }
        return Optional.empty();
    }

    public Set<String> getAltSymbols() {
        return Collections.unmodifiableSet(this.altSymbols);
    }

    public Optional<String> getUcumCode() {
        if (this.ucumCode != null) {
            return Optional.of(ucumCode);
        }
        if (this.isScaled() && this.prefix != null && this.scalingOf.ucumCode != null) {
            return Optional.of(
                    this.prefix.getUcumCode().orElse(this.prefix.getSymbol())
                            + this.scalingOf.getUcumCode().get());
        }
        return Optional.empty();
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        return labels.getLangStringForLanguageTag(languageTag, null, true);
    }

    public Optional<String> getLabelForLanguageTag(
            String language, String fallbackLanguage, boolean allowAnyIfNoMatch) {
        return labels.getStringForLanguageTag(language, fallbackLanguage, allowAnyIfNoMatch);
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

    public int getDependents() {
        return dependents;
    }

    public Set<QuantityKind> getQuantityKinds() {
        return Collections.unmodifiableSet(quantityKinds);
    }

    void addQuantityKind(QuantityKind quantityKind) {
        this.quantityKinds.add(quantityKind);
    }

    public FactorUnits getFactorUnits() {
        return this.factorUnits;
    }

    public Optional<String> getCurrencyCode() {
        return Optional.ofNullable(currencyCode);
    }

    public Optional<Integer> getCurrencyNumber() {
        return Optional.ofNullable(currencyNumber);
    }

    public Set<SystemOfUnits> getUnitOfSystems() {
        return Collections.unmodifiableSet(unitOfSystems);
    }

    void addSystemOfUnits(SystemOfUnits systemOfUnits) {
        Objects.requireNonNull(systemOfUnits);
        this.unitOfSystems.add(systemOfUnits);
    }

    public Set<Unit> getExactMatches() {
        return Collections.unmodifiableSet(this.exactMatches);
    }

    void addExactMatch(Unit exactMatch) {
        Objects.requireNonNull(exactMatch);
        this.exactMatches.add(exactMatch);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isGenerated() {
        return generated;
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
        return QudtNamespaces.unit.abbreviate(this.iri);
    }

    public boolean isCurrencyUnit() {
        return getIriLocalname().startsWith("CCY_");
    }

    public String getIriLocalname() {
        return QudtNamespaces.unit.getLocalName(this.iri);
    }

    public String getIriAbbreviated() {
        return QudtNamespaces.unit.abbreviate(this.iri);
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
