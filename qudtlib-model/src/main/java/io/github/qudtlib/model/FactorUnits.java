package io.github.qudtlib.model;

import static java.util.stream.Collectors.toList;

import io.github.qudtlib.exception.InconvertibleQuantitiesException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FactorUnits {
    private final List<FactorUnit> factorUnits;
    private final BigDecimal scaleFactor;

    public FactorUnits(List<FactorUnit> factorUnits, BigDecimal scaleFactor) {
        this.factorUnits = factorUnits.stream().collect(Collectors.toUnmodifiableList());
        this.scaleFactor = scaleFactor;
    }

    public FactorUnits(List<FactorUnit> factorUnits) {
        this(factorUnits, new BigDecimal("1"));
    }

    public static FactorUnits ofUnit(Unit unit) {
        return new FactorUnits(
                List.of(FactorUnit.builder().unit(Unit.definition(unit)).exponent(1).build()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<FactorUnit> factorUnits = new ArrayList<>();
        private BigDecimal scale = BigDecimal.ONE;

        private Builder() {}

        public Builder factor(Unit unit, int exponent) {
            Objects.requireNonNull(unit);
            this.factor(new FactorUnit(unit, exponent));
            return this;
        }

        public Builder factor(FactorUnit factorUnit) {
            this.factorUnits.add(factorUnit);
            return this;
        }

        public Builder factor(Unit unit) {
            Objects.requireNonNull(unit);
            this.factor(new FactorUnit(unit, 1));
            return this;
        }

        public Builder scaleFactor(BigDecimal scaleFactor) {
            Objects.requireNonNull(scaleFactor);
            this.scale = scaleFactor;
            return this;
        }

        public FactorUnits build() {
            return new FactorUnits(this.factorUnits, this.scale);
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

        return scaleFactor.compareTo(that.scaleFactor) == 0
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
        return new FactorUnits(this.factorUnits, this.scaleFactor.multiply(by));
    }

    public FactorUnits normalize() {
        FactorUnits normalized = FactorUnit.normalizeFactorUnits(this.factorUnits);
        return new FactorUnits(
                normalized.getFactorUnits(),
                normalized.getScaleFactor().multiply(this.scaleFactor));
    }

    public String getDimensionVectorIri() {
        if (this.factorUnits == null || this.factorUnits.isEmpty()) {
            return QudtNamespaces.dimensionVector.makeIriInNamespace("A0E0L0I0M0H0T0D1");
        }
        DimensionVector dv = null;
        for (FactorUnit fu : this.factorUnits) {
            Optional<String> fudvOpt = fu.getDimensionVectorIri();
            if (fudvOpt.isEmpty()) {
                throw new RuntimeException(
                        "Cannot compute dimension vector of factor units as not all units have a dimension vector: "
                                + this.toString());
            }
            if (dv == null) {
                dv = DimensionVector.of(fudvOpt.get());
            } else {
                dv = dv.combine(DimensionVector.of(fudvOpt.get()));
            }
        }
        return dv.getDimensionVectorIri();
    }

    public FactorUnits numerator() {
        return new FactorUnits(
                this.factorUnits.stream().filter(fu -> fu.exponent > 0).collect(toList()));
    }

    public FactorUnits denominator() {
        return new FactorUnits(
                this.factorUnits.stream()
                        .filter(fu -> fu.exponent < 0)
                        .map(fu -> fu.pow(-1))
                        .collect(toList()));
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
        FactorUnits myFactors = this.normalize();
        FactorUnits otherFactors = otherFactorUnits.normalize();
        List<FactorUnit> myFactorUnitList = new ArrayList<>(myFactors.getFactorUnits());
        List<FactorUnit> otherFactorUnitList = new ArrayList<>(otherFactors.getFactorUnits());
        FactorUnit processed = null;
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
                                            .pow(myFactor.exponent, MathContext.DECIMAL128));
                    processed = otherFactor;
                    break;
                }
            }
            if (processed != null) {
                otherFactorUnitList.remove(processed);
                processed = null;
            }
        }
        if (!otherFactorUnitList.isEmpty()) {
            throw new RuntimeException(
                    String.format(
                            "Cannot calculate conversion factor beween factor units %s and %s: %s is unmatched on the right-hand side",
                            myFactors, otherFactors, otherFactorUnitList));
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
                sb.append(symbol).append(getExponentString(fu.exponent)).append("\u00B7");
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
                    sbDenom.append("\u00B7");
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
            sb.append(".");
            sb.append(sbDenom);
        }
        return Optional.of(sb.toString());
    }

    public String getLocalname() {
        StringBuilder sb = new StringBuilder();
        boolean hasDenominator = false;
        for (FactorUnit fu : this.factorUnits) {
            if (fu.exponent > 0) {
                sb.append(getLocalname(fu.unit.getIri()));
                if (fu.exponent > 1) {
                    sb.append(fu.exponent);
                }
                sb.append("-");
            } else {
                hasDenominator = true;
            }
        }
        if (hasDenominator) {
            sb.append("PER-");
        }
        for (FactorUnit fu : this.factorUnits) {
            if (fu.exponent < 0) {
                sb.append(getLocalname(fu.unit.getIri()));
                if (fu.exponent < -1) {
                    sb.append(Math.abs(fu.exponent));
                }
                sb.append("-");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public List<String> generateAllLocalnamePossibilities() {
        List<String> numeratorOptions = permutateFactorUnitLocalnames(fu -> fu.getExponent() > 0);
        List<String> denominatorOptions = permutateFactorUnitLocalnames(fu -> fu.getExponent() < 0);
        List<String> completeOptions = new ArrayList<>();
        for (String numeratorOption : numeratorOptions) {
            for (String denominatorOption : denominatorOptions) {
                StringBuilder completeOption = new StringBuilder();
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
                completeOptions.add(completeOption.toString());
            }
        }
        return completeOptions;
    }

    private List<String> permutateFactorUnitLocalnames(Predicate<FactorUnit> factorUnitPredicate) {
        return permutate(
                        this.factorUnits.stream()
                                .filter(factorUnitPredicate)
                                .map(
                                        fu ->
                                                getLocalname(fu.unit.getIri())
                                                        + (Math.abs(fu.exponent) > 1
                                                                ? Math.abs(fu.exponent)
                                                                : ""))
                                .collect(toList()))
                .stream()
                .map(strings -> strings.stream().collect(Collectors.joining("-")))
                .collect(toList());
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

    private String getExponentString(int exponent) {
        int absExp = Math.abs(exponent);
        switch (absExp) {
            case 1:
                return "";
            case 2:
                return "²";
            case 3:
                return "³";
            case 4:
                return "\u2074";
            case 5:
                return "\u2075";
            case 6:
                return "\u2076";
            case 7:
                return "\u2077";
            default:
                return "m";
        }
    }

    private String getLocalname(String iri) {
        return iri.replaceAll("^.+[/|#]", "");
    }
}
