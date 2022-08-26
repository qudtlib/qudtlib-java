package io.github.qudtlib.model;

import static java.util.stream.Collectors.joining;

import java.math.BigDecimal;
import java.util.*;

public class FactorUnitMatch {
    private final FactorUnit matchedFactorUnit;
    private final List<Unit> matchedPath;
    private final BigDecimal matchedMultiplier;
    private final ScaleFactor scaleFactor;

    public FactorUnitMatch(
            FactorUnit matchedFactorUnit,
            BigDecimal matchedMultiplier,
            Collection<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        Objects.requireNonNull(matchedFactorUnit);
        Objects.requireNonNull(matchedPath);
        Objects.requireNonNull(matchedMultiplier);
        Objects.requireNonNull(scaleFactor);
        this.matchedFactorUnit = matchedFactorUnit;
        this.matchedPath = Collections.unmodifiableList(new ArrayList<>(matchedPath));
        this.matchedMultiplier = matchedMultiplier;
        this.scaleFactor = scaleFactor;
    }

    public FactorUnit getMatchedFactorUnit() {
        return matchedFactorUnit;
    }

    public BigDecimal getMatchedMultiplier() {
        return matchedMultiplier;
    }

    public List<Unit> getMatchedPath() {
        return matchedPath;
    }

    public ScaleFactor getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public String toString() {
        return "FUM{at "
                + Optional.ofNullable(matchedPath).orElse(List.of()).stream()
                        .map(Object::toString)
                        .collect(joining("/"))
                + ", MM{"
                + this.matchedMultiplier
                + "}, "
                + this.scaleFactor
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnitMatch that = (FactorUnitMatch) o;
        return matchedFactorUnit.equals(that.matchedFactorUnit)
                && matchedPath.equals(that.matchedPath)
                && matchedMultiplier.equals(that.matchedMultiplier)
                && scaleFactor.equals(that.scaleFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchedFactorUnit, matchedPath, matchedMultiplier, scaleFactor);
    }
}
