package io.github.qudtlib.model;

import static io.github.qudtlib.math.BigDec.isRelativeDifferenceGreaterThan;
import static java.util.stream.Collectors.toList;

import io.github.qudtlib.exception.IncompleteDataException;
import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import io.github.qudtlib.math.BigDec;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FactorUnits {
    private static final FactorUnits EMPTY_FACTOR_UNITS = new FactorUnits(new ArrayList<>());
    private final List<FactorUnit> factorUnits;
    private final BigDecimal scaleFactor;
    private DimensionVector dimensionVector;
    private transient FactorUnits normalized = null;

    public FactorUnits(
            List<FactorUnit> factorUnits, BigDecimal scaleFactor, String iriForSortingFactors) {
        if (iriForSortingFactors != null) {
            factorUnits = sortAccordingToUnitLocalname(iriForSortingFactors, factorUnits);
        }
        this.factorUnits = normalizeSingleUnitFactors(factorUnits);
        this.scaleFactor = Optional.ofNullable(scaleFactor).orElse(BigDecimal.ONE);
    }

    private static List<FactorUnit> normalizeSingleUnitFactors(List<FactorUnit> factorUnits) {
        if (factorUnits == null) return List.of();
        return factorUnits.stream()
                .map(
                        fu -> {
                            Unit u = fu.getUnit();
                            int exponent = fu.getExponent();
                            // if both the factor unit (fu) and its only factor have exponent != 1,
                            // pull them together
                            // into one factor unit, thus making e.g. (M3=M^3)^-1 -> M^-3
                            if (exponent != 1
                                    && u.hasFactorUnits()
                                    && u.getFactorUnits().getFactorUnits().size() == 1) {
                                FactorUnit onlyFactor = u.getFactorUnits().getFactorUnits().get(0);
                                int factorExponent = onlyFactor.getExponent();
                                if (Math.abs(factorExponent) != 1f) {
                                    return new FactorUnit(
                                            onlyFactor.getUnit(), exponent * factorExponent);
                                }
                            }
                            return fu;
                        })
                .toList();
    }

    public FactorUnits(List<FactorUnit> factorUnits, BigDecimal scaleFactor) {
        this(factorUnits, scaleFactor, null);
    }

    public FactorUnits(List<FactorUnit> factorUnits) {
        this(factorUnits, new BigDecimal("1"));
    }

    public FactorUnits(FactorUnit factorUnit) {
        this(List.of(factorUnit), new BigDecimal("1"));
    }

    public FactorUnits(FactorUnits other) {
        this(other.factorUnits, other.scaleFactor);
    }

    public static FactorUnits ofUnit(Unit unit) {
        return new FactorUnits(
                List.of(FactorUnit.builder().unit(Unit.definition(unit)).exponent(1).build()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderOf(FactorUnits factorUnits) {
        Builder builder = builder();
        builder.scaleFactor(factorUnits.getScaleFactor());
        factorUnits.factorUnits.forEach(fu -> builder.factor(fu));
        return builder;
    }

    public static FactorUnits empty() {
        return EMPTY_FACTOR_UNITS;
    }

    public boolean hasFactorUnits() {
        if (this.factorUnits == null) {
            return false;
        }
        if (this.factorUnits.isEmpty()) {
            return false;
        }
        if (this.factorUnits.size() == 1) {
            Unit u = this.factorUnits.get(0).getUnit();
            if (!this.equals(u.getFactorUnits())) {
                return true;
            }
        }
        if (this.factorUnits.size() == 1
                && this.factorUnits.get(0).getExponent() == 1
                && BigDecimal.ONE.compareTo(this.scaleFactor) == 0) {
            return false;
        }
        return true;
    }

    public boolean isOneOtherUnitWithExponentOne() {
        if (this.factorUnits == null) {
            return false;
        }
        if (this.factorUnits.size() != 1) {
            return false;
        }
        FactorUnit factorUnit = this.factorUnits.get(0);
        if (factorUnit.getExponent() != 1) {
            return false;
        }
        if (this.equals(factorUnit.getUnit().getFactorUnits())) {
            return false;
        }
        return true;
    }

    public static boolean hasFactorUnits(List<FactorUnit> factorUnits) {
        if (factorUnits == null) {
            return false;
        }
        if (factorUnits.isEmpty()) {
            return false;
        }
        if (factorUnits.size() == 1
                && factorUnits.get(0).getUnit().getFactorUnits() != factorUnits) {
            return true;
        }
        if (factorUnits.size() == 1 && factorUnits.get(0).getExponent() == 1) {
            return false;
        }
        return true;
    }

    public boolean hasOneFactorUnit() {
        if (this.factorUnits == null) {
            return false;
        }
        if (this.factorUnits.size() != 1) {
            return false;
        }
        FactorUnit factorUnit = this.factorUnits.get(0);
        if (this.equals(factorUnit.getUnit().getFactorUnits())) {
            return false;
        }
        return true;
    }

    public static class Builder {
        private List<FactorUnit.Builder> factorUnitBuilders = new ArrayList<>();
        private BigDecimal scale = BigDecimal.ONE;

        private String iriForSorting = null;

        private Builder() {}

        public Builder factor(Unit unit, int exponent) {
            Objects.requireNonNull(unit);
            this.factor(new FactorUnit(unit, exponent));
            return this;
        }

        public Builder factor(FactorUnit factorUnit) {
            this.factor(FactorUnit.builder(factorUnit));
            return this;
        }

        public Builder factor(FactorUnit.Builder factorUnitBuilder) {
            this.factorUnitBuilders.add(factorUnitBuilder);
            return this;
        }

        public Builder factor(Unit unit) {
            Objects.requireNonNull(unit);
            this.factor(new FactorUnit(unit, 1));
            return this;
        }

        public Builder iriForSorting(String iriForSorting) {
            this.iriForSorting = iriForSorting;
            return this;
        }

        public Builder scaleFactor(BigDecimal scaleFactor) {
            Objects.requireNonNull(scaleFactor);
            this.scale = scaleFactor;
            return this;
        }

        public FactorUnits build() {
            if (this.iriForSorting != null) {
                return new FactorUnits(
                        factorUnitBuilders.stream()
                                .map(FactorUnit.Builder::build)
                                .collect(toList()),
                        this.scale,
                        iriForSorting);
            }
            return new FactorUnits(
                    factorUnitBuilders.stream().map(FactorUnit.Builder::build).collect(toList()),
                    this.scale);
        }
    }

    /**
     * Accepts up to 7 pairs of &lt;Unit, Integer&gt; which are interpreted as factor units and
     * respective exponents.
     *
     * @param factorUnitSpec array of up to 7 %lt;Unit, Integer%gt; pairs
     * @return true if the specified unit/exponent combination identifies this unit.
     *     (overspecification is counted as a match)
     */
    public static FactorUnits ofFactorUnitSpec(BigDecimal scaleFactor, Object... factorUnitSpec) {
        if (factorUnitSpec.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of arguments is required");
        }
        if (factorUnitSpec.length > 14) {
            throw new IllegalArgumentException(
                    "No more than 14 arguments (7 factor units) supported");
        }
        List<FactorUnit> factorUnits = new ArrayList<>();
        for (int i = 0; i < factorUnitSpec.length; i += 2) {
            Unit requestedUnit;
            requestedUnit = ((Unit) factorUnitSpec[i]);
            Integer requestedExponent = (Integer) factorUnitSpec[i + 1];
            factorUnits.add(
                    FactorUnit.builder()
                            .unit(Unit.definition(requestedUnit))
                            .exponent(requestedExponent)
                            .build());
        }
        return new FactorUnits(factorUnits, scaleFactor);
    }

    public static FactorUnits ofFactorUnitSpec(Object... factorUnitSpec) {
        return ofFactorUnitSpec(BigDecimal.ONE, factorUnitSpec);
    }

    public static FactorUnits ofFactorUnitSpec(
            BigDecimal scaleFactor, Collection<Map.Entry<String, Integer>> factorUnitSpec) {
        Object[] arr = new Object[factorUnitSpec.size() * 2];
        return FactorUnits.ofFactorUnitSpec(
                factorUnitSpec.stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(toList())
                        .toArray(arr));
    }

    public FactorUnits withoutScaleFactor() {
        return new FactorUnits(this.factorUnits);
    }

    public List<FactorUnit> getFactorUnits() {
        return factorUnits;
    }

    public BigDecimal getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnits that = (FactorUnits) o;

        return (!isRelativeDifferenceGreaterThan(
                        scaleFactor, that.scaleFactor, BigDec.ONE_MILLIONTH))
                && new HashSet<>(factorUnits).equals(new HashSet<>(that.factorUnits));
    }

    @Override
    public int hashCode() {
        return Objects.hash(factorUnits, scaleFactor);
    }

    @Override
    public String toString() {
        return (this.scaleFactor.compareTo(BigDecimal.ONE) == 0
                        ? ""
                        : this.scaleFactor.toString() + "*")
                + factorUnits;
    }

    public FactorUnits pow(int exponent) {
        if (exponent == 0) {
            return new FactorUnits(List.of());
        }
        return new FactorUnits(
                this.factorUnits.stream().map(fu -> fu.pow(exponent)).collect(toList()),
                this.scaleFactor.pow(exponent, MathContext.DECIMAL128));
    }

    public FactorUnits combineWith(FactorUnits other) {
        if (other == null) {
            return this;
        }
        return new FactorUnits(
                FactorUnit.contractExponents(
                        Stream.concat(this.factorUnits.stream(), other.factorUnits.stream())
                                .collect(Collectors.toList())),
                this.scaleFactor.multiply(other.scaleFactor, MathContext.DECIMAL128));
    }

    public boolean isRatioOfSameUnits() {
        return this.factorUnits.size() == 2
                && this.factorUnits.get(0).getUnit().equals(this.factorUnits.get(1).getUnit())
                && this.factorUnits.get(0).getExponent()
                        == -1 * this.factorUnits.get(1).getExponent();
    }

    public FactorUnits reduceExponents() {
        return new FactorUnits(FactorUnit.reduceExponents(this.factorUnits), this.scaleFactor);
    }

    public FactorUnits scale(BigDecimal by) {
        return new FactorUnits(
                this.factorUnits, this.scaleFactor.multiply(by, MathContext.DECIMAL128));
    }

    public FactorUnits normalize() {
        if (this.normalized != null) {
            return this.normalized;
        }
        this.normalized = calculateNormalized();
        return this.normalized;
    }

    private FactorUnits calculateNormalized() {
        FactorUnits normalized = FactorUnits.empty();
        if (this.hasFactorUnits()) {
            normalized =
                    this.factorUnits.stream()
                            .map(fu -> fu.getUnit().normalize().pow(fu.getExponent()))
                            .reduce((prev, cur) -> prev.combineWith(cur))
                            .get();
        } else {
            return new FactorUnits(this);
        }
        if (!normalized.isRatioOfSameUnits()) {
            normalized = normalized.reduceExponents();
        }
        return normalized.scale(this.scaleFactor);
    }

    public DimensionVector getDimensionVector() {
        if (this.dimensionVector == null) {
            this.dimensionVector = this.computeDimensionVector();
        }
        return this.dimensionVector;
    }

    private DimensionVector computeDimensionVector() {
        if (this.factorUnits == null || this.factorUnits.isEmpty()) {
            return DimensionVector.DIMENSIONLESS;
        }

        DimensionVector dv = null;
        for (FactorUnit fu : this.factorUnits) {
            Optional<String> fudvOpt = fu.getDimensionVectorIri();
            if (fudvOpt.isEmpty()) {
                throw new IncompleteDataException(
                        String.format(
                                "Cannot compute dimension vector of factor units %s: %s does not have a dimension vector",
                                this.toString(), fu.getUnit().getIriAbbreviated()));
            }
            if (dv == null) {
                dv = DimensionVector.ofRequired(fudvOpt.get());
            } else {
                dv = dv.combine(DimensionVector.ofRequired(fudvOpt.get()));
            }
        }

        return dv;
    }

    public String getDimensionVectorIri() {
        return this.getDimensionVector().getDimensionVectorIri();
    }

    public List<FactorUnit> expand() {
        return streamExpandFactors(this).collect(toList());
    }

    public static Stream<FactorUnit> streamExpandFactors(FactorUnits factorUnits) {
        if (!factorUnits.hasFactorUnits()) {
            return factorUnits.getFactorUnits().stream();
        }
        return factorUnits.getFactorUnits().stream()
                .flatMap(fu -> streamExpandFactors(fu.getUnit().getFactorUnits()));
    }

    /**
     * Returns a FactorUnits object containing the scaleFactor and the factors in the numerator of
     * this FactorUnits object. Note that any derived units in the numerator are returned without
     * recursive decomposition. For example, for `5.0 * M2-PER-N` a FactorUnit object representing
     * `N` is returned.
     *
     * @return a FactorUnits object representing the scaleFactor and the numerator units of this
     *     unit.
     */
    public FactorUnits numerator() {
        return new FactorUnits(numeratorFactors().collect(toList()), this.scaleFactor);
    }

    private Stream<FactorUnit> numeratorFactors() {
        return this.factorUnits.stream().filter(fu -> fu.exponent > 0);
    }

    /**
     * Returns a FactorUnits object containing the factors in the denominator of this FactorUnits
     * object. Note that any derived units in the denominator are returned without recursive
     * decomposition. For example, for `5.0 * ` a FactorUnit object representing `5.0 * N` is
     * returned.
     *
     * @return a FactorUnits object representing the scaleFactor and the numerator units of this
     *     unit.
     */
    public FactorUnits denominator() {
        return new FactorUnits(denominatorFactors().collect(toList()));
    }

    private Stream<FactorUnit> denominatorFactors() {
        return this.factorUnits.stream().filter(fu -> fu.exponent < 0).map(fu -> fu.pow(-1));
    }

    public boolean hasQkdvDenominatorIri(String dimensionVectorIri) {
        return denominator().getDimensionVectorIri().equals(dimensionVectorIri);
    }

    public boolean hasQkdvNumeratorIri(String dimensionVectorIri) {
        return numerator().getDimensionVectorIri().equals(dimensionVectorIri);
    }

    public BigDecimal conversionFactor(Unit other) {
        if (!other.getDimensionVectorIri()
                .map(dv -> dv.equals(this.getDimensionVectorIri()))
                .orElse(false)) {
            throw new InconvertibleQuantitiesException(
                    String.format(
                            "Cannot convert from %s to %s: dimension vectors differ (%s vs %s)",
                            this.toString(),
                            other.getIri(),
                            this.getDimensionVectorIri(),
                            other.getDimensionVectorIri().orElse("[no dimension vector]")));
        }
        FactorUnits otherFactorUnits =
                other.hasFactorUnits()
                        ? new FactorUnits(other.getFactorUnits())
                        : FactorUnits.ofUnit(other);
        return conversionFactorInternal(otherFactorUnits);
    }

    public BigDecimal conversionFactor(FactorUnits otherFactorUnits) {
        if (!otherFactorUnits.getDimensionVectorIri().equals(this.getDimensionVectorIri())) {
            throw new InconvertibleQuantitiesException(
                    String.format(
                            "Cannot convert from %s to %s: dimension vectors differ (%s vs %s)",
                            this.toString(),
                            otherFactorUnits.toString(),
                            this.getDimensionVectorIri(),
                            otherFactorUnits.getDimensionVectorIri()));
        }
        return conversionFactorInternal(otherFactorUnits);
    }

    private BigDecimal conversionFactorInternal(FactorUnits otherFactorUnits) {
        FactorUnits myFactors = this.normalize();
        FactorUnits otherFactors = otherFactorUnits.normalize();
        List<FactorUnit> myFactorUnitList = new ArrayList<>(myFactors.normalize().getFactorUnits());
        List<FactorUnit> otherFactorUnitList =
                new ArrayList<>(otherFactors.normalize().getFactorUnits());
        FactorUnit processed = null;
        if (myFactors.scaleFactor.signum() == 0 || otherFactors.scaleFactor.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal factor =
                myFactors.scaleFactor.divide(otherFactors.scaleFactor, MathContext.DECIMAL128);
        for (FactorUnit myFactor : myFactorUnitList) {
            for (FactorUnit otherFactor : otherFactorUnitList) {
                if (myFactor.unit.isConvertible(otherFactor.unit)
                        && myFactor.exponent == otherFactor.exponent) {
                    factor =
                            factor.multiply(
                                    myFactor.unit
                                            .getConversionMultiplier(otherFactor.unit)
                                            .pow(myFactor.exponent, MathContext.DECIMAL128),
                                    MathContext.DECIMAL128);
                    processed = otherFactor;
                    break;
                }
            }
            if (processed == null) {
                // no match for myFactor in OtherFactorUnitList. If myFactor is D1, multiply it in
                // anyway so we get its conversionMultiplier.
                if (myFactor.getDimensionVectorIri()
                        .map(dv -> DimensionVector.ofRequired(dv).isDimensionless())
                        .orElse(false)) {
                    factor =
                            factor.multiply(
                                    myFactor.getUnit()
                                            .getConversionMultiplier()
                                            .orElse(BigDecimal.ONE),
                                    MathContext.DECIMAL128);
                } else {
                    throw new RuntimeException(
                            String.format(
                                    "Cannot calculate conversion factor beween factor units %s and %s: factor(s) %s of %s is unmatched",
                                    myFactors, otherFactors, myFactor, myFactorUnitList));
                }
            } else {
                otherFactorUnitList.remove(processed);
                processed = null;
            }
        }
        if (!otherFactorUnitList.isEmpty()) {
            List<FactorUnit> unmatchedFactors = new ArrayList<>();
            for (FactorUnit otherFactor : otherFactorUnitList) {
                // no match for myFactor in OtherFactorUnitList. If myFactor is D1, multiply it in
                // anyway so we get its conversionMultiplier.
                if (otherFactor
                        .getDimensionVectorIri()
                        .map(dv -> DimensionVector.ofRequired(dv).isDimensionless())
                        .orElse(false)) {
                    factor =
                            factor.divide(
                                    otherFactor
                                            .getUnit()
                                            .getConversionMultiplier()
                                            .orElse(BigDecimal.ONE),
                                    MathContext.DECIMAL128);
                } else {
                    unmatchedFactors.add(otherFactor);
                }
            }
            if (!unmatchedFactors.isEmpty()) {
                throw new RuntimeException(
                        String.format(
                                "Cannot calculate conversion factor beween factor units %s and %s: factor(s) %s of %s is unmatched ",
                                myFactors, otherFactors, unmatchedFactors, otherFactors));
            }
        }
        return factor;
    }

    public Optional<String> getSymbol() {
        StringBuilder sb = new StringBuilder();
        boolean hasDenominator = false;
        for (FactorUnit fu : this.factorUnits) {
            if (fu.exponent > 0) {
                String symbol = fu.unit.getSymbol().orElse(null);
                if (symbol == null) return Optional.empty();
                sb.append(symbol).append(getExponentString(fu.exponent)).append("·");
            } else {
                hasDenominator = true;
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (hasDenominator) {
            StringBuilder sbDenom = new StringBuilder();
            int cnt = 0;
            for (FactorUnit fu : this.factorUnits) {
                if (fu.exponent < 0) {
                    String symbol = fu.unit.getSymbol().orElse(null);
                    if (symbol == null) return Optional.empty();
                    sbDenom.append(symbol);
                    sbDenom.append(getExponentString(fu.exponent));
                    sbDenom.append("·");
                    cnt++;
                }
            }
            if (sbDenom.length() > 0) {
                sbDenom.deleteCharAt(sbDenom.length() - 1);
            }
            if (cnt > 1) {
                sbDenom.insert(0, "(").append(")");
            }
            sb.append("/");
            sb.append(sbDenom);
        }
        return Optional.of(sb.toString());
    }

    /**
     * Returns the UCUM code if it can be generated (i.e. if the constituent units have ucum codes).
     *
     * @return the ucum code
     */
    public Optional<String> getUcumCode() {
        StringBuilder sb = new StringBuilder();
        boolean hasDenominator = false;
        for (FactorUnit fu : this.factorUnits) {
            if (fu.exponent > 0) {
                String symbol = fu.unit.getUcumCode().orElse(null);
                if (symbol == null) return Optional.empty();
                sb.append(symbol).append(fu.exponent > 1 ? fu.exponent : "").append(".");
            } else {
                hasDenominator = true;
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (hasDenominator) {
            StringBuilder sbDenom = new StringBuilder();
            int cnt = 0;
            for (FactorUnit fu : this.factorUnits) {
                if (fu.exponent < 0) {
                    String symbol = fu.unit.getUcumCode().orElse(null);
                    if (symbol == null) return Optional.empty();
                    sbDenom.append(symbol);
                    sbDenom.append(fu.exponent);
                    sbDenom.append(".");
                    cnt++;
                }
            }
            if (sbDenom.length() > 0) {
                sbDenom.deleteCharAt(sbDenom.length() - 1);
            }
            if (sb.length() > 0) {
                sb.append(".");
            }
            sb.append(sbDenom);
        }
        return Optional.of(sb.toString());
    }

    public String getLocalname() {
        return streamLocalnamePossibilities().findFirst().get();
    }

    public static String getLocalname(List<FactorUnit> factorUnits) {
        return new FactorUnits(factorUnits).streamLocalnamePossibilities().findFirst().get();
    }

    public static List<FactorUnit> sortAccordingToUnitLocalname(
            String unitIriOrAbbreviatedOrLocalName, List<FactorUnit> factorUnitsList) {
        FactorUnits factorUnits = new FactorUnits(factorUnitsList);
        String unitLocalName = unitIriOrAbbreviatedOrLocalName;
        if (QudtNamespaces.unit.isAbbreviatedNamespaceIri(unitLocalName)) {
            unitLocalName = QudtNamespaces.unit.expand(unitLocalName);
        }
        if (QudtNamespaces.unit.isFullNamespaceIri(unitLocalName)) {
            unitLocalName = QudtNamespaces.unit.getLocalName(unitLocalName);
        }
        int perIndex = unitLocalName.indexOf("PER");
        List<FactorUnit> numeratorUnits =
                perIndex == 0
                        ? List.of()
                        : sortBy(
                                factorUnits.numerator(),
                                perIndex == -1
                                        ? unitLocalName
                                        : unitLocalName.substring(0, perIndex));
        List<FactorUnit> denominatorUnits =
                perIndex == -1
                        ? List.of()
                        : sortBy(
                                factorUnits.denominator(),
                                perIndex == 0 ? unitLocalName : unitLocalName.substring(perIndex));
        List<FactorUnit> result =
                Stream.concat(
                                numeratorUnits.stream(),
                                new FactorUnits(denominatorUnits).pow(-1).getFactorUnits().stream())
                        .collect(Collectors.toList());
        if (result.size() != factorUnitsList.size()) {
            // we did not find a match for all units in the label, return original list
            return factorUnitsList;
        }
        return result;
    }

    private static List<FactorUnit> sortBy(FactorUnits factorUnits, String localName) {
        Map<FactorUnit, Integer> orderMap =
                factorUnits.numerator().getFactorUnits().stream()
                        .collect(
                                Collectors.toMap(
                                        fu -> fu,
                                        fu ->
                                                localName.indexOf(
                                                        FactorUnits.getLocalname(List.of(fu))),
                                        (l, r) -> l));
        return factorUnits.getFactorUnits().stream()
                .sorted(Comparator.comparing(factorUnit -> orderMap.get(factorUnit)))
                .collect(Collectors.toList());
    }

    public List<String> generateAllLocalnamePossibilities() {
        return this.streamLocalnamePossibilities().collect(toList());
    }

    public Stream<String> streamLocalnamePossibilities() {
        return streamFactorUnitLocalnames(fu -> fu.getExponent() > 0)
                .flatMap(
                        numeratorOption ->
                                streamFactorUnitLocalnames(fu -> fu.getExponent() < 0)
                                        .map(
                                                denominatorOption -> {
                                                    StringBuilder completeOption =
                                                            new StringBuilder();
                                                    if (numeratorOption.length() > 0) {
                                                        completeOption.append(numeratorOption);
                                                    }
                                                    if (denominatorOption.length() > 0) {
                                                        if (completeOption.length() > 0) {
                                                            completeOption.append("-");
                                                        }
                                                        completeOption.append("PER-");
                                                        completeOption.append(denominatorOption);
                                                    }
                                                    return completeOption.toString();
                                                }));
    }

    private List<String> permutateFactorUnitLocalnames(Predicate<FactorUnit> factorUnitPredicate) {
        return this.streamFactorUnitLocalnames(factorUnitPredicate).collect(toList());
    }

    private Stream<String> streamFactorUnitLocalnames(Predicate<FactorUnit> filterPredicate) {
        return permutate(
                        this.factorUnits.stream()
                                .filter(filterPredicate)
                                .map(
                                        fu ->
                                                getLocalname(fu.unit.getIri())
                                                        + (Math.abs(fu.exponent) > 1
                                                                ? Math.abs(fu.exponent)
                                                                : ""))
                                .collect(toList()))
                .stream()
                .map(strings -> strings.stream().collect(Collectors.joining("-")));
    }

    private List<List<String>> permutate(List<String> strings) {
        List<List<String>> ret = new ArrayList<>();
        if (strings.size() <= 1) {
            ret.add(strings);
            return ret;
        }
        for (int i = 0; i < strings.size(); i++) {
            List<String> otherElements = new ArrayList<>(strings);
            otherElements.remove(i);
            List<List<String>> othersPermutated = permutate(otherElements);
            for (List<String> otherPermutated : othersPermutated) {
                otherPermutated.add(0, strings.get(i));
            }
            ret.addAll(othersPermutated);
        }
        return ret;
    }

    public BigDecimal getConversionMultiplier() {
        return getConversionMultiplierWithFallbackOne();
    }

    public BigDecimal getConversionMultiplierWithFallbackOne() {
        FactorUnits reduced = this.reduceExponents();
        if (reduced.hasFactorUnits()) {
            return reduced.factorUnits.stream()
                    .map(
                            fu ->
                                    fu.unit
                                            .getFactorUnits()
                                            .getConversionMultiplierWithFallbackOne()
                                            .pow(fu.getExponent(), MathContext.DECIMAL128))
                    .reduce((l, r) -> l.multiply(r, MathContext.DECIMAL128))
                    .get()
                    .multiply(reduced.getScaleFactor(), MathContext.DECIMAL128);
        } else {
            if (reduced.factorUnits.isEmpty()) {
                return BigDecimal.ONE;
            }
            return reduced.factorUnits.stream().findFirst().get().conversionMultiplier();
        }
    }

    public Optional<BigDecimal> getConversionMultiplierOpt() {
        FactorUnits reduced =
                this.reduceExponents(); // using normalized factor units helps prevent numeric
        // instability
        if (reduced.hasFactorUnits()) {
            return reduced.factorUnits.stream()
                    .map(
                            fu ->
                                    fu.unit
                                            .getFactorUnits()
                                            .getConversionMultiplierOpt()
                                            .map(
                                                    cm ->
                                                            cm.pow(
                                                                    fu.getExponent(),
                                                                    MathContext.DECIMAL128)))
                    .reduce(
                            (leftOpt, rightOpt) ->
                                    leftOpt.map(
                                            left ->
                                                    rightOpt.map(
                                                                    right ->
                                                                            left.multiply(
                                                                                    right,
                                                                                    MathContext
                                                                                            .DECIMAL128))
                                                            .orElse(null)))
                    .get()
                    .map(cm -> cm.multiply(reduced.getScaleFactor(), MathContext.DECIMAL128));
        } else {
            if (reduced.factorUnits.isEmpty()) {
                return Optional.of(BigDecimal.ONE);
            }
            return reduced.factorUnits.stream()
                    .findFirst()
                    .get()
                    .getUnit()
                    .getConversionMultiplier();
        }
    }

    private String getExponentString(int exponent) {
        int absExp = Math.abs(exponent);

        if (absExp == 1) {
            return "";
        }

        return String.valueOf(absExp)
                .chars()
                .mapToObj(
                        c -> {
                            if (c == 49) {
                                return (char) 185; // Handle 1 to superscript
                            }
                            if (c == 50 || c == 51) {
                                return (char) (c + 128); // Handle 2-3 to superscript
                            } else {
                                return (char) (c + 8256); // Handle 4-0 to superscript
                            }
                        })
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private static String getLocalname(String iri) {
        return iri.replaceAll("^.+[/|#]", "");
    }

    public Stream<FactorUnit> streamAllFactorUnitsRecursively() {
        return streamAllFactorUnitsRecursively(fu -> true);
    }

    public Stream<FactorUnit> streamAllFactorUnitsRecursively(Predicate<FactorUnit> pred) {
        if (this.hasFactorUnits()) {
            return this.factorUnits.stream()
                    .flatMap(fu -> fu.getUnit().getFactorUnits().streamAllFactorUnitsRecursively())
                    .filter(pred);
        } else {
            return this.factorUnits.stream();
        }
    }
}
