package io.github.qudtlib.model;

/**
 * Governs the algorithm used to find units based on their derived units. The DerivedUnitSearchMode
 * is mapped to the {@link FactorUnitMatchingMode} which governs individual unit/factor unit
 * matching.
 */
public enum DerivedUnitSearchMode {
    /** Only select exact matches. */
    EXACT,
    /**
     * Only select exact matches, and if there are multiple matches, select only one of them. The
     * Unit's IRI is used as the tie-breaker, so the result is stable over multiple executions.
     */
    EXACT_ONLY_ONE,
    /**
     * Select exact matches and units whose factor units have different scale, but the scale of the
     * result is equivalent to the cumulative scale of the original factor units
     */
    ALLOW_SCALED,
    /**
     * Select only one unit. Try EXACT mode first. If no match is found, try ALLOW_SCALED. Break
     * ties using the matching units' IRIs.
     */
    BEST_EFFORT_ONLY_ONE;

    public boolean isExactInFirstRound() {
        return this == EXACT || this == EXACT_ONLY_ONE || this == BEST_EFFORT_ONLY_ONE;
    }
}
