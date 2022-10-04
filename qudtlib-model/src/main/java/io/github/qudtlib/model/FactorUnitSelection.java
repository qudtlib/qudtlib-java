package io.github.qudtlib.model;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Set<FactorUnitSelection> removeOverspecifiedMatches(
            Set<FactorUnitSelection> selections) {
        return selections.stream()
                .filter(not(FactorUnitSelection::hasOverspecifiedMatches))
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if a matched selection's path is the parent of another's path.
     *
     * @return true if a matched selection's path is the parent of another's path.
     */
    private boolean hasOverspecifiedMatches() {
        for (int i = 0; i < selectors.size(); i++) {
            FactorUnitSelector sel1 = selectors.get(i);
            if (sel1.getFactorUnitMatch().isPresent()) {
                for (int j = i + 1; j < selectors.size(); j++) {
                    FactorUnitSelector sel2 = selectors.get(j);
                    if (sel2.getFactorUnitMatch().isPresent()) {
                        if (sel1.getFactorUnitMatch()
                                .get()
                                .isOverspecificationWith(sel2.getFactorUnitMatch().get())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public FactorUnitSelection copy() {
        return new FactorUnitSelection(
                this.selectors.stream().map(FactorUnitSelector::copy).collect(Collectors.toList()));
    }

    /**
     * Accepts up to 7 pairs of &lt;Unit, Integer&gt; which are interpreted as factor units and
     * respective exponents.
     *
     * @param factorUnitSpec array of up to 7 %lt;Unit, Integer%gt; pairs
     * @return true if the specified unit/exponent combination identifies this unit.
     *     (overspecification is counted as a match)
     */
    public static FactorUnitSelection fromFactorUnitSpec(Object... factorUnitSpec) {
        if (factorUnitSpec.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of arguments is required");
        }
        if (factorUnitSpec.length > 14) {
            throw new IllegalArgumentException(
                    "No more than 14 arguments (7 factor units) supported");
        }
        List<FactorUnitSelector> selectors = new ArrayList<>();
        for (int i = 0; i < factorUnitSpec.length; i += 2) {
            Unit requestedUnit;
            requestedUnit = ((Unit) factorUnitSpec[i]);
            Integer requestedExponent = (Integer) factorUnitSpec[i + 1];
            selectors.add(new FactorUnitSelector(requestedUnit, requestedExponent));
        }
        return new FactorUnitSelection(selectors);
    }

    public static FactorUnitSelection fromFactorUnitSpec(
            Collection<Map.Entry<String, Integer>> factorUnitSpec) {
        Object[] arr = new Object[factorUnitSpec.size() * 2];
        return FactorUnitSelection.fromFactorUnitSpec(
                factorUnitSpec.stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toArray(arr));
    }

    public List<FactorUnitSelector> getSelectors() {
        return selectors;
    }

    public boolean isSelected(FactorUnit factorUnit, Deque<Unit> checkedPath) {
        return this.selectors.stream()
                .anyMatch(
                        s ->
                                s.getFactorUnitMatch().isPresent()
                                        && factorUnit.equals(
                                                s.getFactorUnitMatch().get().getMatchedFactorUnit())
                                        && Arrays.equals(
                                                checkedPath.toArray(),
                                                s.getFactorUnitMatch()
                                                        .get()
                                                        .getMatchedPath()
                                                        .toArray()));
    }

    public boolean isCompleteMatch() {
        if (!selectors.stream().allMatch(FactorUnitSelector::isBound)) {
            return false;
        }

        Set<ScaleFactor> scaleFactors =
                selectors.stream()
                        .filter(s -> s.getFactorUnitMatch().isPresent())
                        .map(s -> s.getFactorUnitMatch().get().getScaleFactor())
                        .collect(toSet());
        BigDecimal accumulatedScaleFactors =
                scaleFactors.stream()
                        .map(ScaleFactor::getValue)
                        .reduce(BigDecimal.ONE, BigDecimal::multiply);
        BigDecimal acccumulatedMatchedMultipliers =
                selectors.stream()
                        .filter(s -> s.getFactorUnitMatch().isPresent())
                        .map(s -> s.getFactorUnitMatch().get().getMatchedMultiplier())
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
                                        .anyMatch(
                                                u ->
                                                        s.getFactorUnitMatch().isPresent()
                                                                && u.equals(
                                                                        s.getFactorUnitMatch()
                                                                                .get()
                                                                                .getMatchedFactorUnit())));
    }

    public boolean isMatchingSelectorAvailable(FactorUnit factorUnit, int cumulativeExponent) {
        return selectors.stream()
                .anyMatch(s -> s.isAvailable() && s.matches(factorUnit, cumulativeExponent));
    }

    public FactorUnitSelection forPotentialMatch(
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
        return "Select" + selectors;
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
