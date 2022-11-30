package io.github.qudtlib.model;

/** Governs the algorithm used to find units based on their derived units. */
public enum DerivedUnitSearchMode {
    /** Return all matching units. */
    ALL,
    /** Return the best matching unit. */
    BEST_MATCH
}
