package io.github.qudtlib;

import io.github.qudtlib.model.Unit;
import java.util.Comparator;
import java.util.function.Predicate;

/** This enables fast sorting of units by their similarity to a reference unit. */
class UnitDifference {

    Unit unit;
    int difference;

    public UnitDifference(Unit unit, int i) {
        this.unit = unit;
        this.difference = i;
    }

    public Unit getUnit() {
        return unit;
    }

    public static Comparator<UnitDifference> UNIT_DIFF_COMPARATOR =
            (a, b) -> a.difference - b.difference;
    public static Predicate<UnitDifference> UNIT_DIFF_FILTER = a -> a.difference >= 0;
}
