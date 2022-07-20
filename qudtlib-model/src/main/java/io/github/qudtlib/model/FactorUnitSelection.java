package io.github.qudtlib.model;

import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a query by factor units while it is being evaluated.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class FactorUnitSelection {
    private final List<FactorUnitSelector> selectors;

    public FactorUnitSelection(List<FactorUnitSelector> selectors) {
        this.selectors = selectors;
    }

    public FactorUnitSelection copy() {
        return new FactorUnitSelection(
                this.selectors.stream().map(FactorUnitSelector::copy).collect(Collectors.toList()));
    }

    public List<FactorUnitSelector> getSelectors() {
        return selectors;
    }

    public boolean isSelected(FactorUnit factorUnit, Deque<Unit> checkedPath) {
        return this.selectors.stream()
                .anyMatch(
                        s ->
                                factorUnit.equals(s.getMatchedFactorUnit())
                                        && Arrays.equals(
                                                checkedPath.toArray(),
                                                s.getMatchedPath().toArray()));
    }

    public boolean isCompleteMatch() {
        if (!selectors.stream().allMatch(FactorUnitSelector::isBound)) {
            return false;
        }

        Set<ScaleFactor> scaleFactors =
                selectors.stream().map(FactorUnitSelector::getScaleFactor).collect(toSet());
        BigDecimal accumulatedScaleFactors =
                scaleFactors.stream()
                        .map(ScaleFactor::getValue)
                        .reduce(BigDecimal.ONE, BigDecimal::multiply);
        BigDecimal acccumulatedMatchedMultipliers =
                selectors.stream()
                        .map(FactorUnitSelector::getMatchedMultiplier)
                        .reduce(BigDecimal.ONE, BigDecimal::multiply);
        BigDecimal cumulativeScale =
                accumulatedScaleFactors.multiply(acccumulatedMatchedMultipliers);
        return cumulativeScale.compareTo(BigDecimal.ONE) == 0;
    }

    public boolean allMarked(Collection<FactorUnit> factorUnits) {
        return this.selectors.stream()
                .allMatch(
                        s ->
                                factorUnits.stream()
                                        .anyMatch(u -> u.equals(s.getMatchedFactorUnit())));
    }

    public boolean isMatchingSelectorAvailable(FactorUnit factorUnit, int cumulativeExponent) {
        return selectors.stream()
                .anyMatch(s -> s.isAvailable() && s.matches(factorUnit, cumulativeExponent));
    }

    public FactorUnitSelection forMatch(
            FactorUnit factorUnit,
            int cumulativeExponent,
            Deque<Unit> matchedPath,
            ScaleFactor scaleFactor) {
        List<FactorUnitSelector> newSelectors = new ArrayList<>();
        boolean matched = false;
        for (FactorUnitSelector s : this.selectors) {
            if (!matched && s.isAvailable() && s.matches(factorUnit, cumulativeExponent)) {
                matched = true;
                newSelectors.addAll(
                        s.forMatch(factorUnit, cumulativeExponent, matchedPath, scaleFactor));
            } else {
                newSelectors.add(s.copy());
            }
        }
        return new FactorUnitSelection(newSelectors);
    }

    public boolean allBound() {
        return selectors.stream().allMatch(FactorUnitSelector::isBound);
    }

    @Override
    public String toString() {
        return "FUSel{" + selectors + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorUnitSelection selection = (FactorUnitSelection) o;
        return selectors.equals(selection.selectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectors);
    }
}
