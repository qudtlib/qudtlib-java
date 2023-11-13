package io.github.qudtlib;

import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.Unit;

public class QudtEntityAtRuntimeAdder {
    public static void addUnit(Unit unit) {
        Qudt.addUnit(unit);
    }

    public static void addQuantityKind(QuantityKind quantityKind) {
        Qudt.addQuantityKind(quantityKind);
    }
}
