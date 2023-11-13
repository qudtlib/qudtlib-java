package io.github.qudtlib.tools.entitygen;

import io.github.qudtlib.model.DerivedUnitSearchMode;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.Unit;
import io.github.qudtlib.tools.entitygen.model.QuantityKindMetadata;
import io.github.qudtlib.tools.entitygen.model.UnitMetadata;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Tool {
    Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer);

    Unit addDerivedUnit(FactorUnits factorUnits, Consumer<Unit.Definition> unitConfigurer);

    QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer,
            Consumer<QuantityKindMetadata.Builder> metadataConfigurer);

    QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer);

    boolean checkUnitExists(FactorUnits factorUnits);

    boolean checkUnitExists(FactorUnits factorUnits, DerivedUnitSearchMode mode);

    List<Unit> listUnitsWithSameDimensions(Unit unit);

    boolean checkQuantityKindExists(FactorUnits factorUnits);

    List<QuantityKind> searchQuantityKinds(String nameRegex);

    List<QuantityKind> searchQuantityKinds(Predicate<QuantityKind> filter);
}
