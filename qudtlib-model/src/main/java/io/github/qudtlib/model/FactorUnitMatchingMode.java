package io.github.qudtlib.model;

/** Specifies the check whether a Unit matches a given set of factor units. */
public enum FactorUnitMatchingMode {
    /** Only select exact matches */
    EXACT,
    /**
     * Select exact matches and units whose factor units have different scale, but the scale of the
     * result is equivalent to the cumulative scale of the original factor units
     */
    ALLOW_SCALED,
}
