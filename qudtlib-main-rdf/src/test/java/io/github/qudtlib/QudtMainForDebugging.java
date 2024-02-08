package io.github.qudtlib;

import io.github.qudtlib.model.Unit;
import java.util.stream.Collectors;

public class QudtMainForDebugging {
    public static void main(String[] args) {
        System.out.println("Initializing QUDT");
        Unit unit = Qudt.Units.M;
        System.out.println("All qudt units in QUDTLib:");
        System.out.println(
                Qudt.allUnits().stream()
                        .map(Unit::getIriAbbreviated)
                        .collect(Collectors.joining("\n")));
    }
}
