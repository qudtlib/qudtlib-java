package io.github.qudtlib.model;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a query by factor units while it is being evaluated.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class FactorUnitSelection {
    /**
     * The selectors of this selection, defining the individual units that are being searched or
     * have already been found.
     */
    private final List<FactorUnitSelector> selectors;
    /**
     * If the matched units required scaling to match the unit being checked, the scale factor is
     * accumulated in this property.
     */
    private final BigDecimal scaleFactor;

    public FactorUnitSelection(List<FactorUnitSelector> selectors) {
        this(selectors, BigDecimal.ONE);
    }

    public FactorUnitSelection(List<FactorUnitSelector> selectors, BigDecimal scaleFactor) {
        this.selectors = selectors;
        this.scaleFactor = scaleFactor;
    }

    public static FactorUnitSelection fromFactorUnits(List<FactorUnit> factorUnits) {
        return new FactorUnitSelection(
                factorUnits.stream()
                        .map(fu -> new FactorUnitSelector(fu.getUnit(), fu.getExponent()))
                        .collect(toList()));
    }

    /**
     * Returns a new FactorUnitSelection with the same selectors as this one, whose <code>
     * scaleFactor</code> is this FactorUnitSelection's <code>scaleFactor</code> multiplied with the
     * specified one.
     *
     * @param scaleFactor
     * @return
     */
    public FactorUnitSelection scale(BigDecimal scaleFactor) {
        return new FactorUnitSelection(this.selectors, scaleFactor.multiply(this.scaleFactor));
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
                        .collect(toList())
                        .toArray(arr));
    }

    public boolean isCompleteMatch() {
        if (!selectors.stream().allMatch(FactorUnitSelector::isBound)) {
            return false;
        }
        BigDecimal acccumulatedMatchedMultipliers =
                selectors.stream()
                        .filter(s -> s.getFactorUnitMatch().isPresent())
                        .map(s -> s.getFactorUnitMatch().get().getMatchedMultiplier())
                        .reduce(BigDecimal.ONE, BigDecimal::multiply);
        BigDecimal cumulativeScale = this.scaleFactor.multiply(acccumulatedMatchedMultipliers);
        return cumulativeScale.compareTo(BigDecimal.ONE) == 0;
    }

    /**
     * If there are matches for the specified data, return all selections resulting from such
     * matches. If there are no matches, the result is an empty set
     *
     * @param factorUnit the factor unit to match
     * @param cumulativeExponent the exponent accumulated on the path from the root unit to the
     *     factor unit
     * @param matchedPath the path from the root unit to the factor unit
     * @param mode the matching mode
     * @return FactorUnitSelections resulting from the match, empty set if no matches are found.
     */
    public Set<FactorUnitSelection> forPotentialMatch(
            final FactorUnit factorUnit,
            final int cumulativeExponent,
            final Deque<Unit> matchedPath,
            final FactorUnitMatchingMode mode) {
        Set<FactorUnitSelection> newSelections = new HashSet<>();
        // we have to iterate by index as we need to replace selectors, which may exist in multiple,
        // equal instances in the selection (hence the treatment as List, not Set).
        for (int index = 0; index < this.selectors.size(); index++) {
            FactorUnitSelector s = this.selectors.get(index);
            if (s.isAvailable() && s.matches(factorUnit, cumulativeExponent, mode)) {
                List<FactorUnitSelector> origSelectorsWithOneMatch = new ArrayList<>();
                for (int i = 0; i < this.selectors.size(); i++) {
                    if (i == index) {
                        origSelectorsWithOneMatch.addAll(
                                s.forMatch(factorUnit, cumulativeExponent, matchedPath));
                    } else {
                        origSelectorsWithOneMatch.add(this.selectors.get(i));
                    }
                }
                newSelections.add(
                        new FactorUnitSelection(origSelectorsWithOneMatch, this.scaleFactor));
            }
        }
        return newSelections;
    }

    @Override
    public String toString() {
        return (this.scaleFactor.compareTo(BigDecimal.ONE) == 0
                        ? ""
                        : this.scaleFactor.toString() + "*")
                + selectors;
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
